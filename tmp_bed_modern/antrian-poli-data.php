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

function clean_param($value) {
    $value = trim((string) $value);
    return preg_replace('/[^a-zA-Z0-9_. -]/', '', $value);
}

$kd_poli = '';
$kd_dokter = '';

if (!empty($_GET['iyem'])) {
    $token = json_decode(encrypt_decrypt(trim($_GET['iyem']), 'd'), true);
    if (is_array($token)) {
        $kd_poli = isset($token['kd_poli']) ? clean_param($token['kd_poli']) : '';
        $kd_dokter = isset($token['kd_dokter']) ? clean_param($token['kd_dokter']) : '';
    }
} else {
    $kd_poli = isset($_GET['kd_poli']) ? clean_param($_GET['kd_poli']) : '';
    $kd_dokter = isset($_GET['kd_dokter']) ? clean_param($_GET['kd_dokter']) : '';
}

$today = date('Y-m-d');
$filterReg = "";
$filterAntri = "";
if ($kd_poli !== '') {
    $filterReg .= " and reg_periksa.kd_poli='".$kd_poli."'";
    $filterAntri .= " and antripoli.kd_poli='".$kd_poli."'";
}
if ($kd_dokter !== '') {
    $filterReg .= " and reg_periksa.kd_dokter='".$kd_dokter."'";
    $filterAntri .= " and antripoli.kd_dokter='".$kd_dokter."'";
}

try {
    $current = array();
    $newCalls = array();

    $currentSql = "
        select
            antripoli.no_rawat,
            antripoli.kd_poli,
            antripoli.kd_dokter,
            antripoli.status as status_panggil,
            reg_periksa.no_reg,
            pasien.nm_pasien,
            poliklinik.nm_poli,
            dokter.nm_dokter
        from antripoli
        inner join reg_periksa on antripoli.no_rawat=reg_periksa.no_rawat
        inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis
        inner join poliklinik on antripoli.kd_poli=poliklinik.kd_poli
        inner join dokter on antripoli.kd_dokter=dokter.kd_dokter
        where reg_periksa.tgl_registrasi='".$today."' ".$filterAntri."
        order by antripoli.status desc, reg_periksa.no_reg
    ";
    $result = bukaquery($currentSql);
    while ($row = mysqli_fetch_assoc($result)) {
        $isNew = $row['status_panggil'] === '1';
        $item = array(
            'no_rawat' => $row['no_rawat'],
            'no_reg' => $row['no_reg'],
            'pasien' => $row['nm_pasien'],
            'kd_poli' => $row['kd_poli'],
            'poli' => $row['nm_poli'],
            'kd_dokter' => $row['kd_dokter'],
            'dokter' => $row['nm_dokter'],
            'baru' => $isNew
        );
        $current[] = $item;
        if ($isNew) {
            $newCalls[] = $item;
        }
    }

    $queueSql = "
        select
            reg_periksa.no_reg,
            reg_periksa.no_rawat,
            reg_periksa.kd_poli,
            reg_periksa.kd_dokter,
            pasien.nm_pasien,
            poliklinik.nm_poli,
            dokter.nm_dokter
        from reg_periksa
        inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis
        inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli
        inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter
        where reg_periksa.tgl_registrasi='".$today."' and reg_periksa.stts='Belum' ".$filterReg."
        order by poliklinik.nm_poli, dokter.nm_dokter, reg_periksa.no_reg
    ";

    $groups = array();
    $totalWaiting = 0;
    $result = bukaquery($queueSql);
    while ($row = mysqli_fetch_assoc($result)) {
        $key = $row['kd_poli'].'|'.$row['kd_dokter'];
        if (!isset($groups[$key])) {
            $groups[$key] = array(
                'kd_poli' => $row['kd_poli'],
                'poli' => $row['nm_poli'],
                'kd_dokter' => $row['kd_dokter'],
                'dokter' => $row['nm_dokter'],
                'total' => 0,
                'items' => array()
            );
        }

        $groups[$key]['total']++;
        $totalWaiting++;
        if (count($groups[$key]['items']) < 6) {
            $groups[$key]['items'][] = array(
                'no_reg' => $row['no_reg'],
                'no_rawat' => $row['no_rawat'],
                'pasien' => $row['nm_pasien']
            );
        }
    }

    if (count($newCalls) > 0) {
        foreach ($newCalls as $call) {
            bukaquery2("update antripoli set status='0' where kd_poli='".$call['kd_poli']."' and kd_dokter='".$call['kd_dokter']."' and no_rawat='".$call['no_rawat']."'");
        }
    }

    json_response(array(
        'status' => 'ok',
        'generated_at' => date('Y-m-d H:i:s'),
        'display_time' => date('d M Y H:i'),
        'scope' => array(
            'kd_poli' => $kd_poli,
            'kd_dokter' => $kd_dokter
        ),
        'totals' => array(
            'waiting' => $totalWaiting,
            'current' => count($current),
            'clinics' => count($groups)
        ),
        'current' => $current,
        'new_calls' => $newCalls,
        'groups' => array_values($groups)
    ));
} catch (Throwable $e) {
    json_response(array(
        'status' => 'error',
        'message' => 'Data antrian poli belum dapat dibaca.',
        'detail' => $e->getMessage()
    ), 500);
}
