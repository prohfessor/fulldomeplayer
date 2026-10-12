Плеер планетария
==================

Актуальная документация находится в README.md.

Сборка:
    C:\opt\player\cdnd.bat

Запуск:
    C:\opt\player\player.bat

Полная проверка:
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File C:\opt\player\verify.ps1

Настройки находятся в player.properties. Web-часть проекта:
https://github.com/prohfessor/fulldome-web-player

ВАЖНО: текущая сборка встроенного Chromium/JCEF не поддерживает MP4
(H.264/AAC). До выпуска JCEF со встроенной поддержкой кодеков используйте
видео WebM (VP8/VP9 + Opus/Vorbis). Установка Chrome или Chromium в Windows
не добавляет поддержку MP4 встроенному браузеру плеера.
