<?php
require_once('conf/conf.php');
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
header('Pragma: no-cache');
date_default_timezone_set('Asia/Makassar');

$setting = mysqli_fetch_array(bukaquery("select nama_instansi, alamat_instansi, kabupaten, propinsi, kontak, email, logo from setting"));
$logo = isset($setting['logo']) ? base64_encode($setting['logo']) : '';
$iyem = isset($_GET['iyem']) ? trim($_GET['iyem']) : '';
?>
<!doctype html>
<html lang="id">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Antrian Poli - <?php echo htmlspecialchars($setting['nama_instansi']); ?></title>
    <style>
        :root {
            --ink: #122033;
            --muted: #607086;
            --panel: rgba(255,255,255,.90);
            --line: rgba(148,163,184,.35);
            --brand: #155e75;
            --accent: #0f766e;
            --call: #f97316;
            --green: #16a34a;
            --surface: #f5f8fc;
            --glow: rgba(249,115,22,.42);
        }

        body.dark {
            color-scheme: dark;
            --ink: #e8f2ff;
            --muted: #9fb1c8;
            --panel: rgba(15,23,42,.84);
            --line: rgba(148,163,184,.24);
            --brand: #38bdf8;
            --accent: #5eead4;
            --call: #fb923c;
            --green: #4ade80;
            --surface: #07111f;
            --glow: rgba(251,146,60,.45);
        }

        * { box-sizing: border-box; }

        body {
            margin: 0;
            min-height: 100vh;
            overflow: hidden;
            font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
            color: var(--ink);
            background:
                radial-gradient(circle at 12% 10%, rgba(20,184,166,.18), transparent 28%),
                radial-gradient(circle at 88% 8%, rgba(249,115,22,.18), transparent 32%),
                linear-gradient(135deg, #f7fbff, #eef6f7);
        }

        body.dark {
            background:
                radial-gradient(circle at 18% 18%, rgba(20,184,166,.18), transparent 30%),
                radial-gradient(circle at 80% 10%, rgba(249,115,22,.18), transparent 34%),
                linear-gradient(135deg, #07111f, #0f172a 52%, #111827);
        }

        .screen {
            height: 100vh;
            display: grid;
            grid-template-rows: auto 1fr auto;
            gap: 18px;
            padding: 22px;
        }

        .topbar {
            display: grid;
            grid-template-columns: auto 1fr auto;
            align-items: center;
            gap: 18px;
        }

        .logo {
            width: 88px;
            height: 88px;
            border-radius: 16px;
            object-fit: cover;
            background: #fff;
            box-shadow: 0 18px 48px rgba(15,23,42,.18);
        }

        h1 {
            margin: 0;
            font-size: clamp(40px, 4.4vw, 76px);
            line-height: 1;
        }

        .subtitle {
            margin-top: 8px;
            color: var(--muted);
            font-size: clamp(17px, 1.25vw, 24px);
            font-weight: 650;
        }

        .clock {
            min-width: 245px;
            padding: 14px 18px;
            border-radius: 8px;
            border: 1px solid var(--line);
            background: var(--panel);
            text-align: right;
            box-shadow: 0 18px 48px rgba(15,23,42,.12);
        }

        .clock strong {
            display: block;
            color: var(--accent);
            font-size: clamp(34px, 3vw, 54px);
            line-height: 1;
        }

        .clock span {
            display: block;
            margin-top: 8px;
            color: var(--muted);
            font-size: 14px;
            font-weight: 750;
        }

        .toolbar {
            display: flex;
            gap: 8px;
            margin-top: 10px;
        }

        button {
            height: 34px;
            padding: 0 14px;
            border: 1px solid var(--line);
            border-radius: 999px;
            background: rgba(255,255,255,.66);
            color: var(--ink);
            font-weight: 850;
            cursor: pointer;
        }

        body.dark button {
            background: rgba(15,23,42,.72);
        }

        .main {
            min-height: 0;
            display: grid;
            grid-template-columns: minmax(520px, .95fr) 1.35fr;
            gap: 18px;
        }

        .panel {
            min-height: 0;
            overflow: hidden;
            border: 1px solid var(--line);
            border-radius: 8px;
            background: var(--panel);
            box-shadow: 0 18px 48px rgba(15,23,42,.12);
        }

        .call-panel {
            display: grid;
            grid-template-rows: auto 1fr auto;
        }

        .section-title {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 16px 18px;
            border-bottom: 1px solid var(--line);
            font-size: clamp(20px, 1.4vw, 28px);
            font-weight: 900;
        }

        .section-title span {
            color: var(--muted);
            font-size: clamp(14px, 1vw, 18px);
        }

        .current {
            display: grid;
            place-items: center;
            padding: 24px;
            text-align: center;
        }

        .queue-number {
            font-size: clamp(110px, 12vw, 220px);
            line-height: .9;
            font-weight: 950;
            color: var(--call);
            animation: callGlow 2.4s ease-in-out infinite;
            text-shadow: 0 0 28px var(--glow);
        }

        @keyframes callGlow {
            0%, 100% { filter: brightness(1); transform: scale(1); }
            50% { filter: brightness(1.18); transform: scale(1.018); }
        }

        .patient {
            margin-top: 18px;
            font-size: clamp(30px, 3.2vw, 58px);
            font-weight: 900;
            max-width: 100%;
            overflow-wrap: anywhere;
        }

        .destination {
            margin-top: 16px;
            color: var(--muted);
            font-size: clamp(22px, 2vw, 36px);
            font-weight: 800;
        }

        .doctor {
            margin-top: 6px;
            color: var(--brand);
            font-size: clamp(18px, 1.5vw, 28px);
            font-weight: 850;
        }

        .stats {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            border-top: 1px solid var(--line);
        }

        .stat {
            padding: 16px;
            border-right: 1px solid var(--line);
        }

        .stat:last-child { border-right: 0; }
        .stat b {
            display: block;
            font-size: clamp(32px, 3vw, 56px);
            line-height: 1;
        }
        .stat span {
            color: var(--muted);
            font-size: 14px;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: .08em;
        }

        .groups {
            height: calc(100% - 62px);
            overflow: hidden;
            padding: 14px;
            position: relative;
        }

        .grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 12px;
        }

        .clinic {
            min-height: 184px;
            padding: 16px;
            border-radius: 8px;
            background: rgba(255,255,255,.70);
            border: 1px solid var(--line);
        }

        body.dark .clinic {
            background: rgba(15,23,42,.70);
        }

        .clinic-head {
            display: flex;
            justify-content: space-between;
            gap: 12px;
            align-items: flex-start;
        }

        .clinic h3 {
            margin: 0;
            font-size: clamp(22px, 1.75vw, 34px);
            line-height: 1.08;
        }

        .clinic .doc {
            margin-top: 6px;
            color: var(--muted);
            font-size: clamp(14px, 1.05vw, 20px);
            font-weight: 750;
        }

        .count {
            min-width: 58px;
            height: 48px;
            display: grid;
            place-items: center;
            border-radius: 999px;
            background: rgba(22,163,74,.14);
            color: var(--green);
            font-size: 26px;
            font-weight: 950;
        }

        .next-list {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            margin-top: 16px;
        }

        .ticket {
            min-width: 66px;
            padding: 8px 12px;
            border-radius: 999px;
            background: rgba(21,94,117,.12);
            color: var(--brand);
            text-align: center;
            font-size: clamp(20px, 1.45vw, 30px);
            font-weight: 950;
        }

        .empty {
            height: 100%;
            display: grid;
            place-items: center;
            color: var(--muted);
            font-size: 30px;
            font-weight: 900;
            text-align: center;
            padding: 24px;
        }

        .footer {
            display: flex;
            justify-content: space-between;
            gap: 18px;
            padding: 13px 16px;
            border-radius: 8px;
            color: white;
            background: linear-gradient(90deg, #155e75, #0f766e);
            font-weight: 850;
            box-shadow: 0 18px 48px rgba(15,23,42,.15);
        }

        .dot {
            display: inline-block;
            width: 10px;
            height: 10px;
            margin-right: 8px;
            border-radius: 50%;
            background: #86efac;
            box-shadow: 0 0 0 6px rgba(134,239,172,.18);
        }

        @media (max-width: 1200px) {
            body { overflow: auto; }
            .screen { height: auto; min-height: 100vh; }
            .topbar, .main { grid-template-columns: 1fr; }
            .clock { text-align: left; }
            .grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body class="dark">
    <div class="screen">
        <header class="topbar">
            <?php if ($logo !== '') { ?>
                <img class="logo" src="data:image/jpeg;base64,<?php echo $logo; ?>" alt="Logo">
            <?php } ?>
            <div>
                <h1>Antrian Poli</h1>
                <div class="subtitle"><?php echo htmlspecialchars($setting['nama_instansi'].' - '.$setting['alamat_instansi'].', '.$setting['kabupaten']); ?></div>
            </div>
            <div class="clock">
                <strong id="clock">--:--:--</strong>
                <span id="dateLabel">Memuat tanggal</span>
                <div class="toolbar">
                    <button id="modeToggle" type="button">Mode Terang</button>
                    <button id="voiceToggle" type="button">Suara Aktif</button>
                </div>
            </div>
        </header>

        <main class="main">
            <section class="panel call-panel">
                <div class="section-title">Panggilan Sekarang <span id="currentCount">0 aktif</span></div>
                <div class="current" id="currentCall">
                    <div class="empty">Belum ada panggilan poli</div>
                </div>
                <div class="stats">
                    <div class="stat"><b id="waitingCount">0</b><span>Menunggu</span></div>
                    <div class="stat"><b id="clinicCount">0</b><span>Poli Aktif</span></div>
                    <div class="stat"><b id="calledCount">0</b><span>Dipanggil</span></div>
                </div>
            </section>

            <section class="panel">
                <div class="section-title">Antrian Berikutnya <span id="updatedAt">-</span></div>
                <div class="groups" id="groupsWrap">
                    <div class="empty">Memuat daftar antrian</div>
                </div>
            </section>
        </main>

        <footer class="footer">
            <div><span class="dot"></span><span id="connection">Terhubung ke SIMRS</span></div>
            <div>Data otomatis diperbarui setiap 5 detik</div>
        </footer>
    </div>

    <audio id="bell" src="bell.wav" preload="auto"></audio>

    <script>
        const iyem = <?php echo json_encode($iyem); ?>;
        const dataUrl = 'antrian-poli-data.php' + (iyem ? '?iyem=' + encodeURIComponent(iyem) + '&' : '?');
        const dayNames = ['Minggu','Senin','Selasa','Rabu','Kamis','Jumat','Sabtu'];
        const monthNames = ['Januari','Februari','Maret','April','Mei','Juni','Juli','Agustus','September','Oktober','November','Desember'];
        const calledKeys = new Set();
        let voiceEnabled = localStorage.getItem('poliVoice') !== 'off';

        function text(id, value) {
            document.getElementById(id).textContent = value;
        }

        function escapeHtml(value) {
            return String(value ?? '').replace(/[&<>"']/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[char]));
        }

        function tickClock() {
            const now = new Date();
            text('clock', now.toLocaleTimeString('id-ID', { hour12: false }).replaceAll('.', ':'));
            text('dateLabel', `${dayNames[now.getDay()]}, ${now.getDate()} ${monthNames[now.getMonth()]} ${now.getFullYear()}`);
        }

        function applyTheme(theme) {
            document.body.classList.toggle('dark', theme === 'dark');
            text('modeToggle', theme === 'dark' ? 'Mode Terang' : 'Mode Gelap');
            localStorage.setItem('poliTheme', theme);
        }

        function applyVoiceLabel() {
            text('voiceToggle', voiceEnabled ? 'Suara Aktif' : 'Suara Mati');
            localStorage.setItem('poliVoice', voiceEnabled ? 'on' : 'off');
        }

        function renderCurrent(items) {
            const target = document.getElementById('currentCall');
            text('currentCount', `${items.length} aktif`);

            if (!items.length) {
                target.innerHTML = '<div class="empty">Belum ada panggilan poli</div>';
                return;
            }

            const item = items[0];
            target.innerHTML = `
                <div>
                    <div class="queue-number">${escapeHtml(item.no_reg)}</div>
                    <div class="patient">${escapeHtml(item.pasien)}</div>
                    <div class="destination">${escapeHtml(item.poli)}</div>
                    <div class="doctor">${escapeHtml(item.dokter)}</div>
                </div>
            `;
        }

        function renderGroups(groups) {
            const target = document.getElementById('groupsWrap');
            if (!groups.length) {
                target.innerHTML = '<div class="empty">Belum ada pasien menunggu</div>';
                return;
            }

            target.innerHTML = `<div class="grid">${groups.map(group => `
                <article class="clinic">
                    <div class="clinic-head">
                        <div>
                            <h3>${escapeHtml(group.poli)}</h3>
                            <div class="doc">${escapeHtml(group.dokter)}</div>
                        </div>
                        <div class="count">${group.total}</div>
                    </div>
                    <div class="next-list">
                        ${group.items.map(item => `<span class="ticket">${escapeHtml(item.no_reg)}</span>`).join('')}
                    </div>
                </article>
            `).join('')}</div>`;
        }

        function announce(call) {
            const key = `${call.kd_poli}|${call.kd_dokter}|${call.no_rawat}`;
            if (calledKeys.has(key)) {
                return;
            }
            calledKeys.add(key);

            const bell = document.getElementById('bell');
            bell.currentTime = 0;
            bell.play().catch(() => {});

            if (!voiceEnabled || !('speechSynthesis' in window)) {
                return;
            }

            const message = `Nomor antrian ${call.no_reg}, menuju ${call.poli}.`;
            const utterance = new SpeechSynthesisUtterance(message);
            utterance.lang = 'id-ID';
            utterance.rate = 0.92;
            utterance.pitch = 1;
            window.speechSynthesis.cancel();
            window.speechSynthesis.speak(utterance);
        }

        async function loadData() {
            try {
                const response = await fetch(dataUrl + 'ts=' + Date.now(), { cache: 'no-store' });
                const payload = await response.json();
                if (payload.status !== 'ok') {
                    throw new Error(payload.message || 'Data tidak tersedia');
                }

                text('waitingCount', payload.totals.waiting);
                text('clinicCount', payload.totals.clinics);
                text('calledCount', payload.totals.current);
                text('updatedAt', payload.display_time);
                text('connection', 'Terhubung ke SIMRS');
                renderCurrent(payload.current);
                renderGroups(payload.groups);
                payload.new_calls.forEach(announce);
            } catch (error) {
                text('connection', 'Data belum dapat dimuat');
            }
        }

        document.getElementById('modeToggle').addEventListener('click', () => {
            applyTheme(document.body.classList.contains('dark') ? 'light' : 'dark');
        });

        document.getElementById('voiceToggle').addEventListener('click', () => {
            voiceEnabled = !voiceEnabled;
            applyVoiceLabel();
        });

        tickClock();
        applyTheme(localStorage.getItem('poliTheme') || 'dark');
        applyVoiceLabel();
        loadData();
        setInterval(tickClock, 1000);
        setInterval(loadData, 5000);
    </script>
</body>
</html>
