# AlgorithmCLOPE

AlgorithmCLOPE - это консольное приложение, реализующее механизм кластеризации CLOPE (подробнее на сайте https://loginom.ru/blog/clope), для обработки массимов категорийных и транзакционных данных.

## Стэк

Spring, JPA, Maven, PostgreSQL, Docker, Liquibase.

## Основные команды

import data / initialization / iteration / print / clear / stop / exit

### Команда `import data`

При запуске команды начинается импорт данных из файла `input/agaricus-lepiota.data` (либо другого пути указанного вами).
При вводе данной команды следущим шагом необходимо ввести абсолютный путь до вашего файла с исходными данными либо нажать enter для импорта из файла по умолчанию.

### Команда `initialization`

При запуске команды необходимо дополнительно указать параметр repulsion для определения плотности кластеризации, после чего запускается процесс обработки данных из базы данных.
Обработка идет "в один проход", тоесть после того как все записи в таблице базы данных будут обработаны по одному разу обработка прекратится.
Для выполнения необходим успешно проведенный импорт данных в базу данных.

### Команда `iteration`

При запуске команды необходимо дополнительно указать параметр `repulsion` для определения плотности кластеризации и параметр `calculation error` для указания погрешности кластеризации, после чего запускается процесс обработки данных из базы данных.
Обработка ведется до тех пор пока абсолютная выгода кластеризации не будет меньше `calculation error`.
Для выполнения необходим успешно проведенный импорт данных в базу данных.

### Команда `print`

Команда предназначена для наглядного представление данных по итогам обработки.
После выполнения кластеризации возможен вывод в консоль информации о количестве транзакций в каждом кластере, группированно по типу одного элемента, для чего необходимо указать номер i-того элемента.

### Команда `clear`

Команда предназначна для очистки данных кластеризации в базе данных и оперативной памяти.
Рекомендуется выполнять перед началом кластеризации.

### Команда `stop`

Команда предназначения для остановки инициализации или итерации кластеризации данных. При остановке вычисленний данной командой не происходит потери данных и возможно продожение вычислений начиная с момента остановки (даже после потери данных из оперативной памяти при условии сохранения данных в базе данных).

### Команда `exit`

Команда предназначана для корретной остановки работы приложения (при выполнении также запускает команду `stop`).

## Конфигурационный файл приложения - `application.yaml`

### Раздел `server`

В этом разделе задаётся параметр port — порт, через который контроллеры приложения "слушают" веб-запросы.

### Раздел `spring`

Данное приложение по умолчанию работает с СУБД PostgreSQL, работа с другим базами данных типа SQL возможна при изменении соответствующих настроек в файле конфигурации, зависимостей в файле pom.xml и параметров в файле db.changelog-master.xml.
Кроме того используется система версионирования базы данных Liquibase при помощи который создается схема базы данных.

## Запуск программы

Перед запуском программы убедитесь, что выполнены следующие шаги:

- Запустите базу данных `clope`.
   Для упрощения создание самой базы данных имеется возможность создания контейнера Docker из файла конфигурации `docker-compose.yml`.
   Для это из корня дирректории проекта через терминал запустите команду `docker-compose up -d`.
   Схемы и таблицы базы данных создаютя автоматически при помощи системы версионирования баз данных Liquibase.

Теперь приложение можно компилировать и запускать из программной среды.