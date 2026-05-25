<?php
require_once('conf/conf.php');
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
header('Pragma: no-cache');
date_default_timezone_set('Asia/Makassar');

$setting = mysqli_fetch_array(bukaquery("select nama_instansi, alamat_instansi, kabupaten, propinsi, kontak, email, logo from setting"));
$logo = isset($setting['logo']) ? base64_encode($setting['logo']) : '';
?>
<!doctype html>
<html lang="id">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Ketersediaan Bed - <?php echo htmlspecialchars($setting['nama_instansi']); ?></title>
    <style>
        :root {
            color-scheme: light;
            --ink: #13202f;
            --muted: #627083;
            --line: #d9e2ec;
            --panel: rgba(255,255,255,.88);
            --brand: #0f766e;
            --brand-strong: #115e59;
            --available: #16a34a;
            --used: #dc2626;
            --other: #d97706;
            --surface: #f5f7fb;
            --glow: rgba(20, 184, 166, .36);
        }

        body.dark {
            color-scheme: dark;
            --ink: #e8f0f8;
            --muted: #9fb0c4;
            --line: rgba(148,163,184,.22);
            --panel: rgba(15,23,42,.82);
            --brand: #2dd4bf;
            --brand-strong: #5eead4;
            --available: #4ade80;
            --used: #fb7185;
            --other: #fbbf24;
            --surface: #07111f;
            --glow: rgba(94, 234, 212, .34);
        }

        * { box-sizing: border-box; }

        body {
            margin: 0;
            min-height: 100vh;
            font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
            color: var(--ink);
            background:
                linear-gradient(135deg, rgba(15,118,110,.18), rgba(37,99,235,.08) 42%, rgba(245,158,11,.14)),
                var(--surface);
            overflow: hidden;
        }

        body.dark {
            background:
                radial-gradient(circle at 16% 18%, rgba(45,212,191,.20), transparent 30%),
                radial-gradient(circle at 82% 12%, rgba(59,130,246,.20), transparent 32%),
                linear-gradient(135deg, #07111f, #0f172a 48%, #111827);
        }

        .screen {
            min-height: 100vh;
            display: grid;
            grid-template-rows: auto auto 1fr auto;
            gap: 16px;
            padding: 22px;
        }

        .topbar {
            display: grid;
            grid-template-columns: auto 1fr auto;
            gap: 18px;
            align-items: center;
        }

        .logo {
            width: 92px;
            height: 92px;
            border-radius: 16px;
            object-fit: cover;
            background: white;
            border: 1px solid rgba(255,255,255,.72);
            box-shadow: 0 18px 50px rgba(17,24,39,.16);
        }

        .identity h1 {
            margin: 0;
            font-size: clamp(34px, 4vw, 68px);
            line-height: 1;
            letter-spacing: 0;
        }

        .identity p {
            margin: 8px 0 0;
            color: var(--muted);
            font-size: clamp(17px, 1.35vw, 24px);
        }

        .clock {
            min-width: 240px;
            padding: 16px 20px;
            border-radius: 8px;
            background: var(--panel);
            border: 1px solid rgba(255,255,255,.7);
            box-shadow: 0 18px 50px rgba(17,24,39,.12);
            text-align: right;
        }

        .clock strong {
            display: block;
            font-size: clamp(30px, 3vw, 48px);
            line-height: 1;
            color: var(--brand-strong);
        }

        .clock span {
            display: block;
            margin-top: 8px;
            color: var(--muted);
            font-size: 15px;
        }

        .mode-toggle {
            margin-top: 12px;
            width: 100%;
            height: 38px;
            border: 1px solid var(--line);
            border-radius: 999px;
            background: rgba(255,255,255,.72);
            color: var(--ink);
            font-weight: 800;
            cursor: pointer;
        }

        body.dark .mode-toggle {
            background: rgba(15,23,42,.72);
        }

        .summary {
            display: grid;
            grid-template-columns: 1.3fr repeat(3, 1fr);
            gap: 14px;
        }

        .metric {
            min-height: 148px;
            padding: 22px;
            border-radius: 8px;
            background: var(--panel);
            border: 1px solid rgba(255,255,255,.75);
            box-shadow: 0 18px 50px rgba(17,24,39,.10);
        }

        .metric .label {
            color: var(--muted);
            font-size: clamp(15px, 1vw, 20px);
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: .08em;
        }

        .metric .value {
            margin-top: 8px;
            font-size: clamp(58px, 6.2vw, 108px);
            line-height: .95;
            font-weight: 850;
            animation: numberGlow 2.8s ease-in-out infinite;
            text-shadow: 0 0 0 transparent;
        }

        .metric .note {
            margin-top: 8px;
            color: var(--muted);
            font-size: clamp(16px, 1.1vw, 22px);
        }

        .metric.available .value { color: var(--available); }
        .metric.used .value { color: var(--used); }
        .metric.other .value { color: var(--other); }

        @keyframes numberGlow {
            0%, 100% {
                filter: brightness(1);
                text-shadow: 0 0 0 transparent;
            }
            50% {
                filter: brightness(1.16);
                text-shadow:
                    0 0 16px var(--glow),
                    0 0 34px var(--glow);
            }
        }

        .content {
            min-height: 0;
            display: grid;
            grid-template-columns: 390px 1fr;
            gap: 18px;
        }

        .panel {
            min-height: 0;
            border-radius: 8px;
            background: var(--panel);
            border: 1px solid rgba(255,255,255,.78);
            box-shadow: 0 18px 50px rgba(17,24,39,.11);
            overflow: hidden;
        }

        .panel-title {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            padding: 16px 18px;
            border-bottom: 1px solid var(--line);
        }

        .panel-title h2 {
            margin: 0;
            font-size: clamp(20px, 1.35vw, 28px);
            letter-spacing: 0;
        }

        .panel-title span {
            color: var(--muted);
            font-weight: 700;
            font-size: clamp(14px, .95vw, 18px);
        }

        .class-list {
            height: calc(100% - 58px);
            overflow: hidden;
            padding: 12px;
        }

        .class-card {
            display: grid;
            grid-template-columns: 1fr auto;
            gap: 8px 12px;
            padding: 14px;
            margin-bottom: 10px;
            border-radius: 8px;
            background: #fff;
            border: 1px solid #e5edf5;
        }

        body.dark .class-card {
            background: rgba(15,23,42,.86);
            border-color: rgba(148,163,184,.22);
        }

        .class-card strong {
            font-size: clamp(22px, 1.55vw, 30px);
        }

        .class-card .beds {
            font-size: clamp(34px, 2.45vw, 48px);
            font-weight: 850;
            color: var(--available);
        }

        .bar {
            grid-column: 1 / -1;
            height: 10px;
            overflow: hidden;
            border-radius: 999px;
            background: #e8eef5;
        }

        body.dark .bar {
            background: rgba(148,163,184,.20);
        }

        .bar span {
            display: block;
            height: 100%;
            border-radius: inherit;
            background: linear-gradient(90deg, var(--used), #f59e0b);
            width: 0%;
            transition: width .45s ease;
        }

        .class-meta {
            grid-column: 1 / -1;
            display: flex;
            justify-content: space-between;
            color: var(--muted);
            font-size: clamp(14px, 1vw, 18px);
            font-weight: 700;
        }

        .table-wrap {
            height: calc(100% - 58px);
            overflow: hidden;
            position: relative;
            scroll-behavior: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            table-layout: fixed;
        }

        thead th {
            position: sticky;
            top: 0;
            z-index: 1;
            background: #f8fbfd;
            color: var(--muted);
            font-size: clamp(14px, .95vw, 18px);
            text-transform: uppercase;
            letter-spacing: .07em;
            text-align: left;
            padding: 16px 18px;
            border-bottom: 1px solid var(--line);
        }

        body.dark thead th {
            background: rgba(15,23,42,.94);
        }

        tbody td {
            padding: 18px 18px;
            border-bottom: 1px solid #e9eff5;
            font-size: clamp(23px, 1.75vw, 34px);
            font-weight: 750;
            vertical-align: middle;
        }

        tbody tr:nth-child(even) {
            background: rgba(248,251,253,.74);
        }

        body.dark tbody tr:nth-child(even) {
            background: rgba(30,41,59,.45);
        }

        .num {
            text-align: center;
            font-variant-numeric: tabular-nums;
        }

        .room-name {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .pill {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 74px;
            height: 46px;
            padding: 0 16px;
            border-radius: 999px;
            font-weight: 850;
            background: #e8f7ee;
            color: var(--available);
        }

        body.dark .pill {
            background: rgba(74,222,128,.14);
        }

        .pill.used {
            background: #fee2e2;
            color: var(--used);
        }

        body.dark .pill.used {
            background: rgba(251,113,133,.16);
        }

        .footer {
            display: flex;
            justify-content: space-between;
            gap: 18px;
            padding: 12px 16px;
            border-radius: 8px;
            color: white;
            background: linear-gradient(90deg, var(--brand-strong), #1d4ed8);
            font-weight: 750;
            box-shadow: 0 18px 50px rgba(17,24,39,.14);
        }

        .status-dot {
            display: inline-block;
            width: 10px;
            height: 10px;
            margin-right: 8px;
            border-radius: 999px;
            background: #86efac;
            box-shadow: 0 0 0 6px rgba(134,239,172,.18);
        }

        .empty {
            display: grid;
            place-items: center;
            height: 100%;
            color: var(--muted);
            font-size: 24px;
            font-weight: 800;
        }

        @media (max-width: 980px) {
            body { overflow: auto; }
            .screen { padding: 14px; }
            .topbar, .content, .summary { grid-template-columns: 1fr; }
            .clock { text-align: left; min-width: 0; }
            .content { min-height: 800px; }
            .footer { flex-direction: column; }
        }

        @media (max-width: 1280px) {
            .occupancy-col {
                display: none;
            }
        }
    </style>
</head>
<body>
    <div class="screen">
        <header class="topbar">
            <?php if ($logo !== '') { ?>
                <img class="logo" src="data:image/jpeg;base64,<?php echo $logo; ?>" alt="Logo">
            <?php } ?>
            <div class="identity">
                <h1>Ketersediaan Bed Rawat Inap</h1>
                <p><?php echo htmlspecialchars($setting['nama_instansi'].' - '.$setting['alamat_instansi'].', '.$setting['kabupaten']); ?></p>
            </div>
            <div class="clock">
                <strong id="clock">--:--:--</strong>
                <span id="dateLabel">Memuat tanggal</span>
                <button class="mode-toggle" id="modeToggle" type="button">Mode Gelap</button>
            </div>
        </header>

        <section class="summary" aria-label="Ringkasan bed">
            <div class="metric">
                <div class="label">Total Bed Aktif</div>
                <div class="value" id="totalBed">0</div>
                <div class="note"><span id="occupancy">0%</span> okupansi saat ini</div>
            </div>
            <div class="metric available">
                <div class="label">Bed Kosong</div>
                <div class="value" id="emptyBed">0</div>
                <div class="note">Siap digunakan</div>
            </div>
            <div class="metric used">
                <div class="label">Bed Terisi</div>
                <div class="value" id="usedBed">0</div>
                <div class="note">Pasien dirawat</div>
            </div>
            <div class="metric other">
                <div class="label">Status Lain</div>
                <div class="value" id="otherBed">0</div>
                <div class="note">Reservasi, rusak, atau lainnya</div>
            </div>
        </section>

        <main class="content">
            <section class="panel">
                <div class="panel-title">
                    <h2>Per Kelas</h2>
                    <span id="classCount">0 kelas</span>
                </div>
                <div class="class-list" id="classList">
                    <div class="empty">Memuat data</div>
                </div>
            </section>

            <section class="panel">
                <div class="panel-title">
                    <h2>Ruang Rawat Inap</h2>
                    <span id="roomCount">0 ruang</span>
                </div>
                <div class="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 46%">Ruang</th>
                                <th class="num" style="width: 14%">Total</th>
                                <th class="num" style="width: 14%">Terisi</th>
                                <th class="num" style="width: 14%">Kosong</th>
                                <th class="num occupancy-col" style="width: 12%">Okupansi</th>
                            </tr>
                        </thead>
                        <tbody id="roomRows">
                            <tr><td colspan="5"><div class="empty">Memuat data</div></td></tr>
                        </tbody>
                    </table>
                </div>
            </section>
        </main>

        <footer class="footer">
            <div><span class="status-dot"></span><span id="connection">Terhubung ke SIMRS</span></div>
            <div>Pembaruan terakhir: <span id="updatedAt">-</span></div>
        </footer>
    </div>

    <script>
        const formatNumber = new Intl.NumberFormat('id-ID');
        const dayNames = ['Minggu','Senin','Selasa','Rabu','Kamis','Jumat','Sabtu'];
        const monthNames = ['Januari','Februari','Maret','April','Mei','Juni','Juli','Agustus','September','Oktober','November','Desember'];
        const modeToggle = document.getElementById('modeToggle');

        function text(id, value) {
            document.getElementById(id).textContent = value;
        }

        function escapeHtml(value) {
            return String(value ?? '').replace(/[&<>"']/g, function (char) {
                return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[char]);
            });
        }

        function tickClock() {
            const now = new Date();
            const clock = now.toLocaleTimeString('id-ID', { hour12: false });
            const date = `${dayNames[now.getDay()]}, ${now.getDate()} ${monthNames[now.getMonth()]} ${now.getFullYear()}`;
            text('clock', clock.replaceAll('.', ':'));
            text('dateLabel', date);
        }

        function applyTheme(theme) {
            document.body.classList.toggle('dark', theme === 'dark');
            modeToggle.textContent = theme === 'dark' ? 'Mode Terang' : 'Mode Gelap';
            localStorage.setItem('bedDashboardTheme', theme);
        }

        function renderClasses(classes) {
            const target = document.getElementById('classList');
            text('classCount', `${classes.length} kelas`);

            if (!classes.length) {
                target.innerHTML = '<div class="empty">Belum ada data kelas</div>';
                return;
            }

            target.innerHTML = classes.map(item => `
                <article class="class-card">
                    <strong>${escapeHtml(item.kelas)}</strong>
                    <div class="beds">${formatNumber.format(item.kosong)}</div>
                    <div class="bar"><span style="width:${item.okupansi}%"></span></div>
                    <div class="class-meta">
                        <span>${formatNumber.format(item.isi)} terisi / ${formatNumber.format(item.total)} bed</span>
                        <span>${item.okupansi}%</span>
                    </div>
                </article>
            `).join('');
        }

        function renderRooms(rooms) {
            const target = document.getElementById('roomRows');
            text('roomCount', `${rooms.length} ruang`);

            if (!rooms.length) {
                target.innerHTML = '<tr><td colspan="5"><div class="empty">Belum ada data ruang</div></td></tr>';
                return;
            }

            target.innerHTML = rooms.map(item => `
                <tr>
                    <td><div class="room-name">${escapeHtml(item.ruang)}</div></td>
                    <td class="num">${formatNumber.format(item.total)}</td>
                    <td class="num"><span class="pill used">${formatNumber.format(item.isi)}</span></td>
                    <td class="num"><span class="pill">${formatNumber.format(item.kosong)}</span></td>
                    <td class="num occupancy-col">${item.okupansi}%</td>
                </tr>
            `).join('');
        }

        function setupAutoScroll() {
            const wrap = document.querySelector('.table-wrap');
            if (!wrap) {
                return;
            }

            let direction = 1;
            let pauseUntil = 0;
            let pixelCarry = 0;
            const speed = 0.55;

            wrap.addEventListener('mouseenter', () => {
                pauseUntil = Date.now() + 60000;
            });

            wrap.addEventListener('mouseleave', () => {
                pauseUntil = Date.now() + 1200;
            });

            function step() {
                if (Date.now() >= pauseUntil && wrap.scrollHeight > wrap.clientHeight + 8) {
                    pixelCarry += speed;
                    const move = Math.floor(pixelCarry);

                    if (move >= 1) {
                        pixelCarry -= move;
                        wrap.scrollTop += move * direction;
                    }

                    if (wrap.scrollTop + wrap.clientHeight >= wrap.scrollHeight - 2) {
                        direction = -1;
                        pauseUntil = Date.now() + 1800;
                    } else if (wrap.scrollTop <= 0) {
                        direction = 1;
                        pauseUntil = Date.now() + 1800;
                    }
                }

                requestAnimationFrame(step);
            }

            requestAnimationFrame(step);
        }

        async function loadBedData() {
            try {
                const response = await fetch('bed-modern-data.php?ts=' + Date.now(), { cache: 'no-store' });
                const payload = await response.json();

                if (payload.status !== 'ok') {
                    throw new Error(payload.message || 'Data tidak tersedia');
                }

                text('totalBed', formatNumber.format(payload.totals.bed));
                text('emptyBed', formatNumber.format(payload.totals.kosong));
                text('usedBed', formatNumber.format(payload.totals.isi));
                text('otherBed', formatNumber.format(payload.totals.lainnya));
                text('occupancy', `${payload.totals.okupansi}%`);
                text('updatedAt', payload.display_time);
                text('connection', 'Terhubung ke SIMRS');

                renderClasses(payload.classes);
                renderRooms(payload.rooms);
            } catch (error) {
                text('connection', 'Data belum dapat dimuat');
                text('updatedAt', '-');
            }
        }

        tickClock();
        applyTheme(localStorage.getItem('bedDashboardTheme') || 'light');
        modeToggle.addEventListener('click', () => {
            applyTheme(document.body.classList.contains('dark') ? 'light' : 'dark');
        });
        setupAutoScroll();
        loadBedData();
        setInterval(tickClock, 1000);
        setInterval(loadBedData, 10000);
    </script>
</body>
</html>
