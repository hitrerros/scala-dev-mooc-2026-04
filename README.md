# Otus Scala Developer Course 2026-04

Учебный репозиторий для курса Scala Developer на платформе Otus (запуск апрель 2026).

В этом репозитории размещаются исходные коды лекций и материалы для выполнения домашних заданий в рамках курса.

## Важно: Работа с репозиторием

**Студенты не должны работать напрямую с этим репозиторием через GitHub (не создавать PR или issues здесь).**

Вам необходимо создать собственную копию (форк) этого репозитория и работать в ней.

## Инструкция по началу работы

### 1. Создание форка на GitHub

1. Перейдите на страницу репозитория курса: `https://github.com/[organization]/scala-dev-mooc-2026-04`
2. В правом верхнем углу нажмите кнопку **Fork**
3. Выберите свой аккаунт как владельца форка
4. Нажмите **Create fork**

Теперь у вас есть собственная копия репозитория: `https://github.com/[ваш-username]/scala-dev-mooc-2026-04`

### 2. Клонирование форка на локальную машину

```bash
# Клонируйте ВАШ форк (не оригинальный репозиторий)
git clone git@github.com:[ваш-username]/scala-dev-mooc-2026-04.git
cd scala-dev-mooc-2026-04

# Добавьте ссылку на оригинальный репозиторий как upstream
# Это понадобится для получения обновлений
git remote add upstream git@github.com:ScalaOtus/scala-dev-mooc-2026-04.git

# Проверьте настроенные remote
git remote -v
```

Вы должны увидеть:
```
origin    git@github.com:[ваш-username]/scala-dev-mooc-2026-04.git (fetch)
origin    git@github.com:[ваш-username]/scala-dev-mooc-2026-04.git (push)
upstream  git@github.com:ScalaOtus/scala-dev-mooc-2026-04.git (fetch)
upstream  git@github.com:ScalaOtus/scala-dev-mooc-2026-04.git (push)
```

### 3. Получение обновлений из основного репозитория

По мере прохождения курса в основном репозитории могут появляться обновления (новые материалы, исправления). Для синхронизации вашего форка:

```bash
# Перейдите в ветку main/master
git checkout main  # или git checkout master

# Загрузите изменения из upstream
git fetch upstream

# Слейте изменения из upstream/main в вашу локальную ветку
git merge upstream/main

# Отправьте обновления в свой форк на GitHub
git push origin main
```

**Рекомендуется делать это перед началом работы над каждым новым домашним заданием.**

### 4. Работа над домашними заданиями

Для каждого домашнего задания создавайте отдельную ветку в вашем форке:

```bash
# Убедитесь, что у вас последние изменения
git checkout main
git pull upstream main  # получить обновления из курса
git push origin main     # отправить в свой форк

# Создайте ветку для домашнего задания
git checkout -b homework-01-structure

# Выполняйте задание, коммитьте изменения
git add .
git commit -m "Решение домашнего задания 01"

# Отправьте ветку в свой форк
git push origin homework-01-structure
```

### 5. Создание Pull Request в вашем форке

После завершения домашнего задания:

1. Перейдите в ваш форк на GitHub
2. Нажмите **Compare & pull request** для созданной ветки
3. Заполните название и описание PR
4. Нажмите **Create pull request**

**Важно:** Pull Request создаётся в вашем форке для демонстрации выполнения задания. Не создавайте PR в основном репозитории курса.

## Полезные команды

```bash
# Просмотр всех веток
git branch -a

# Просмотр разницы между ветками
git diff main homework-01-structure

# Удаление локальной ветки после merging
git branch -d homework-01-structure

# Отмена изменений в файле
git checkout -- filename.scala
```

## Структура репозитория

```
scala-dev-mooc-2026-04/
├── src/main/scala/ru/otus/
│   └── module1/          # Материалы модуля 1
│       ├── 01-control-structures.scala
│       └── 01-functions.scala
├── README.md
└── build.sbt
```

## Требования к окружению

- Scala 3.x (версия будет уточнена в начале курса)
- JDK 17 или выше
- SBT (Scala Build Tool) — для сборки проектов

## Контакты и поддержка

По всем вопросам, связанным с курсом, используйте чат курса в VK.

---

**Удачи в изучении Scala!**
