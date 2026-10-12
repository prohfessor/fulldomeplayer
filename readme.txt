
cdnd.bat - компиляция плейера (и остальных примеров)
player.bat - запуск

команда "C:\Users\new\Downloads\exiftool -XMP-GSpherical:Spherical="true" файл.mp4" - задает метатег 360 для mp4
Взято отсюда: https://p.cweiske.de/621
также посмотреть здесь: https://www.youtube.com/watch?v=a7M5cztMCz8&t=72s&ab_channel=IvanAlchimista

java 18

Для webm нужно запустить ubuntu, зайти на диск винды (/mnt/c) и запустить команду:
 "mkvpropedit --edit track:v1 --set projection-type=1 asdfasdf.webm"
Взято отсюда: https://groups.google.com/g/spatial-media-discuss/c/GTTGniCKSmU

todo:
1. -- Длина видео отдельной цифрой.
2. Проверка диска H, проверка nginx
3. файл настроек (текущие диски, порты...)
4. не работает mute. Сделать движок громкости и mute
5. проверить запуск видео с дисков
6. убрать 

13.04.25
ExtendedDnDDemo.java:main() установил локаль:
Locale.setDefault(Locale.US);
чтобы в поле ввода разделитель был точкой 3333.3


21.12.24
в RangeSliderDemo.java вставил флаг flagFromTable, чтобы не было закциливания при выборе видео в плейлисте. Не доделано. 

Когда тыкаем в плейлисте на видео, вызываются:
463		slider.setBeginTime(min);
464		slider.setEndTime(max);

Цепочка вызовов при setEndTime(max)
                                                           текст перемотки
ExtendedDnDDemo.java:listener -> slider.setEndTime(max) -> videoEnd.setText () -> rangeSlider.setUpperValue -> rangeSlider.addChangeListener -> rangeSlider.videoEnd.setText
                                                                                    player.setEndTime                                           videoBegin.setText

08.12.24
1. Длина видео отдельной цифрой. Работает при любом нажатии (1, 2) на редакторе плейлиста.
2. Защита от затирания измененного плейлиста - кнопка "редактировать плейлист".
3. Изменение таблицы вынесено в отдельную функцию 
	public void settable(JTable table_playlist_editor, File cur_script_name) {
ее причесать и использовать в просмотрщике плейлистов (справа-внизу).


01.12.24
Время видео:                                                                                                                                                              текстовое поле перемотки           движок перемотки
1. FulldomeRenderer:animate() -> index.html:sendMessage("cmd:currenttime;", curtime) ->  MainFrame.java:ExtendedDnDDemo.setTime(curtime) -> slider.setCurTime(curtime) -> videoSet.setText()  -> listener -> slider_set.setValue()
ввел flagVideoSet, чтобы slider_set.setValue() не вызывал videoSet.setText(), что приводило к зацикливанию
Время видео посылается браузером, затем приходит на текстовое поле перемотки, которое посылает команду слайдеру перемотки. Т.к. у обоих есть слушатель, выключаем его в слайдере.
2. Перемотка слайдером
ExtendedDnDDemo.setCurTime(time) ->
В RangeSliderDemo.setCurTime() устанавливается flagVideoPosFromBrowser в true, когда приходит значение переметки из браузера (чтобы videoSet (текстовое поле) не посылала обратно в браузер)

videoSet (текстовое поле) делаем главным (управляющим), т.к. в нем значение == float, в отличие от движка slider_set (слайдер)

поправил установку конца видео
c:\utl\convall.bat - конвертирует все mp4 файлы в webm
o:\h - в эту папку копирую все видеофайлы. mp4 кодирую в webm
делаем новую версию библиотеки фильмов

24.11.24
RangeSliderDemo.java:148 - зацикливание. Когда с браузера идет счет времени, в этом месте идет команда браузеру.

22.11.24
Когда тыкаем в файл напрямую (не добавляя в плейлист), можно менять все параметры. Но тогда имеет смысл заблокировать интерфейс, чтобы нельзя
было выбрать другой файл (из плейлистов, из ФС, из текущего плейлиста), пока не отреагировал на запрос на сохранение файла в плейлисте. 
Сделать запрос на сохранения параметра 360 в файл?
По умолчанию, все файлы считаются fulldome, т.к это обычная плоская картинка. 

Добавил чекбокс переключения 360 или fulldome

15.11.24
Привел в порядок перемотку видео.
Добавил еще один ползунок сверху ползунка начала и конец видео.

Привязать к перемотке "extent" - когда тыкаешь по линии.

10.11.24
Убрал необходимость добавления урл к player.bat - по умолчанию загружается localhost/pl/index.html
Добавил ползунки поворота, тангажа, вверх-вниз, приближения. 
Поворот и тангаж добавляются в ползунок - ExtendedDnDDemo.java:391

Сделать:
Данные из ползунков добавлять в плейлист
Разобраться с ползунком перемотки
Сделать проверку работы nginx. От встроенного вебсервера отказался - nginx самый быстрый. Jetty - тормознее.
Чтобы поворты были плавнее, возможно имеет смысл в ползунках задавать значения в 10 раз больше, преобразуя обратно в тексте - т.е. DefaultBoundedRangeModel(0,0, -1800, 1800)



03.11.24
переноска файлов из списка файлов в редактор.
Проверка на тип файла происходит в классе:
"class TransferableTreeNod extends TreePath implements":328:DndTree.java
TransferableTreeNode - не используется, т.к. работает только с именем файла (без пути)

Проверили на куполе (h:/opt/player)
Чтобы увидеть на куполе - поменять последний false на true в строке ExtendedDnDDemo.java:822
	        childFrame = new MainFrame(cefApp_, "http://localhost/pl/index.html", useOsr, false, true);
	                                                                                             -----
h: диск подмонтировали subst 
"subst h: e:\html"

20.10.24
https://icons8.ru/icons/set/stop-button - иконки
https://ernienotes.wordpress.com/2010/12/27/creating-a-java-swing-range-slider/ - слайдер
https://www.frolov-lib.ru/programming/javasamples/index.html - примеры на яве
Сделал слайд перемотки и кнопки play

https://www.frolov-lib.ru/programming/javasamples/vol5/vol5_6/index.html - описание gridBagLayout
https://ipsoftware.ru/posts/gridbaglayout/ - здесь дополнения
https://www.onlinetutorialspoint.com/java/java-swing-borderfactory-example.html - border (с надписями)


08.09.24
не работает посылка сообщений в хром

07.09.24
Умеем посылать информацию из хрома в java. 
для теста:
FullDomeRenderer.js:519 - setTimes() - каждый кадр посылает время в java
setTimes() - описана в index.html:74, 

19.05.24
541:     protected void importString(JComponent c, String str) { // в этом месте вставляем строку в таблицу при бросании при drag&drop
1. добавить путь к файлу


18.05.24
строка 148: При клике на имя плейлиста на правой консоли, выводим содержимое в консоль
Надо вставить это содержимое на правой-нижней панели

17.05.24
основано на C:\opt\test\dnd\ExtendedDnDDemo.java
C:\opt\test\ext_dnd.bat 

