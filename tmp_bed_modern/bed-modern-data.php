<?php
require_once('conf/conf.php');

header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
header('Pragma: no-cache');
date_default_timezone_set('Asia/Makassar');

function json_response($payload, $status = 200) {
    http_response_code($status);
    echo json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

$rooms = array();
$classes = array();
$totals = array(
    'bed' => 0,
    'isi' => 0,
    'kosong' => 0,
    'lainnya' => 0
);

try {
    $query = "
        select
            bangsal.kd_bangsal,
            bangsal.nm_bangsal,
            kamar.kelas,
            count(kamar.kd_kamar) as total_bed,
            sum(case when kamar.status='ISI' then 1 else 0 end) as bed_isi,
            sum(case when kamar.status='KOSONG' then 1 else 0 end) as bed_kosong,
            sum(case when kamar.status not in ('ISI','KOSONG') then 1 else 0 end) as bed_lainnya
        from kamar
        inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal
        where kamar.statusdata='1' and bangsal.status='1'
        group by bangsal.kd_bangsal, bangsal.nm_bangsal, kamar.kelas
        order by bangsal.nm_bangsal, kamar.kelas
    ";

    $result = bukaquery($query);
    while ($row = mysqli_fetch_assoc($result)) {
        $total = (int) $row['total_bed'];
        $isi = (int) $row['bed_isi'];
        $kosong = (int) $row['bed_kosong'];
        $lainnya = (int) $row['bed_lainnya'];
        $kelas = $row['kelas'];

        $rooms[] = array(
            'kode' => $row['kd_bangsal'],
            'ruang' => $row['nm_bangsal'],
            'kelas' => $kelas,
            'total' => $total,
            'isi' => $isi,
            'kosong' => $kosong,
            'lainnya' => $lainnya,
            'okupansi' => $total > 0 ? round(($isi / $total) * 100) : 0
        );

        if (!isset($classes[$kelas])) {
            $classes[$kelas] = array(
                'kelas' => $kelas,
                'total' => 0,
                'isi' => 0,
                'kosong' => 0,
                'lainnya' => 0
            );
        }

        $classes[$kelas]['total'] += $total;
        $classes[$kelas]['isi'] += $isi;
        $classes[$kelas]['kosong'] += $kosong;
        $classes[$kelas]['lainnya'] += $lainnya;

        $totals['bed'] += $total;
        $totals['isi'] += $isi;
        $totals['kosong'] += $kosong;
        $totals['lainnya'] += $lainnya;
    }

    foreach ($classes as $key => $class) {
        $classes[$key]['okupansi'] = $class['total'] > 0 ? round(($class['isi'] / $class['total']) * 100) : 0;
    }

    $totals['okupansi'] = $totals['bed'] > 0 ? round(($totals['isi'] / $totals['bed']) * 100) : 0;

    json_response(array(
        'status' => 'ok',
        'generated_at' => date('Y-m-d H:i:s'),
        'display_time' => date('d M Y H:i'),
        'totals' => $totals,
        'classes' => array_values($classes),
        'rooms' => $rooms
    ));
} catch (Throwable $e) {
    json_response(array(
        'status' => 'error',
        'message' => 'Data ketersediaan bed belum dapat dibaca.',
        'detail' => $e->getMessage()
    ), 500);
}
