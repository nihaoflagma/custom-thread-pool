

# 🧵 Thread Pool

Этот проект представляет собой **пользовательскую реализацию пула потоков (ThreadPool)**, предназначенную для управления задачами в многопоточном высоконагруженном серверном приложении.

В отличие от стандартного `ThreadPoolExecutor`, реализация предоставляет:

- Расширенную конфигурацию  
- Логику отказа  
- Кастомный `ThreadFactory`  
- Систему логирования всех ключевых этапов работы пула

---

## 📑 Содержание

- [Архитектура](#архитектура)
- [Основные компоненты](#основные-компоненты)
- [Параметры конфигурации](#параметры-конфигурации)
- [Обработка отказов](#обработка-отказов)
- [Распределение задач](#распределение-задач)
- [Логирование](#логирование)
- [Интерфейс управления](#интерфейс-управления)
- [Демонстрационная программа](#демонстрационная-программа)
- [Анализ и производительность](#анализ-и-производительность)
- [Заключение](#заключение)

---

## 🏗 Архитектура

Проект построен вокруг собственной реализации пула потоков. Цель — **гибко управлять количеством потоков, их жизненным циклом, очередями задач и отказами при перегрузке**.

Пул использует:

- Рабочие потоки (`Worker`), каждый из которых слушает свою очередь задач
- `ThreadFactory`, создающий именованные потоки и логирующий события
- Очередь задач, ограниченная по размеру
- Механизм завершения потоков при бездействии (`keepAliveTime`)

---

## ⚙️ Основные компоненты

- `CustomThreadPool`: основной класс пула, реализующий интерфейс `CustomExecutor`
- `Worker`: класс, реализующий поток, обрабатывающий задачи из очереди
- `ThreadFactory`: фабрика потоков, назначающая имена и логирующая создание/завершение
- `RejectedExecutionHandler`: логика отказа при переполнении очереди
- `Main`: демонстрационный запуск с имитационными задачами

---

## 📌 Параметры конфигурации

| Параметр         | Назначение                                    |
|------------------|-----------------------------------------------|
| `corePoolSize`   | Минимальное количество активных потоков       |
| `maxPoolSize`    | Максимальное количество потоков               |
| `keepAliveTime`  | Время жизни простаивающего потока             |
| `timeUnit`       | Единица измерения времени ожидания            |
| `queueSize`      | Максимальный размер очереди задач             |
| `minSpareThreads`| Минимальное число свободных потоков в ожидании|

---

## 🚫 Обработка отказов

Если все потоки заняты, а очередь задач переполнена:

- Новая задача **отклоняется**
- В лог выводится сообщение:

```

\[Rejected] Task ... was rejected due to overload!

````

> 📌 Почему выбран отказ?  
> Это позволяет не блокировать вызывающий поток и явно сигнализировать об угрозе перегрузки системы. Такой подход безопасен в системах, где стабильность важнее, чем обработка каждой задачи.

---

## ⚖️ Распределение задач

- Задачи добавляются в общую очередь
- Рабочие потоки забирают их по мере готовности

Дополнительно можно:

- Расширить реализацию на Round-Robin или Least Loaded стратегию
- Распараллелить очереди для каждого потока при необходимости

---

## 📝 Логирование

Подробное логирование всех этапов работы пула:

| Событие             | Пример лога                                      |
|---------------------|--------------------------------------------------|
| Создание потока     | `[ThreadFactory] Creating new thread: MyPool-worker-1` |
| Завершение потока   | `[Worker] MyPool-worker-1 terminated.`           |
| Принятие задачи     | `[Pool] Task accepted into queue: <task>`        |
| Выполнение задачи   | `[Worker] Thread-2 executes <task>`              |
| Завершение задачи   | `[Task] Finish task 2 in Thread-2`               |
| Простой и завершение| `[Worker] Thread-2 idle timeout, stopping.`      |
| Отклонение задачи   | `[Rejected] Task ... was rejected due to overload!` |

---

## 🧩 Интерфейс управления

```java
interface CustomExecutor extends Executor {
    void execute(Runnable command);
    <T> Future<T> submit(Callable<T> callable);
    void shutdown();
    void shutdownNow();
}
````

✔ Совместима с:

* `CompletableFuture`
* Стандартными механизмами `Executor`

---

## 🧪 Демонстрационная программа

**Файл:** `Main.java`

### Конфигурация пула:

```java
corePoolSize = 2;
maxPoolSize = 4;
queueSize = 5;
keepAliveTime = 5 секунд;
```

### Запуск 10 задач:

```java
Runnable task = () -> {
    System.out.println("[Task] Start ...");
    Thread.sleep(2000);
    System.out.println("[Task] Finish ...");
};
```

* Переполнение очереди приведёт к отказу задач
* После завершения всех задач вызывается `shutdown()`

---

## 📊 Анализ и производительность

| Характеристика                 | CustomThreadPool | ThreadPoolExecutor |
| ------------------------------ | ---------------- | ------------------ |
| Конфигурация `minSpareThreads` | ✅ Да             | ❌ Нет              |
| Кастомное логирование          | ✅ Да             | 🔘 Ограничено      |
| Поддержка отказов              | ✅ Да             | ✅ Да               |
| Расширяемость стратегии        | ✅ Высокая        | 🔘 Средняя         |
| Реализация `shutdown()`        | ✅ Да             | ✅ Да               |

---

## 🧠 Заключение

Эта реализация кастомного пула потоков демонстрирует:

* Полный контроль над созданием и управлением потоками
* Реалистичную обработку перегрузок
* Расширяемость и читаемое логирование
* Совместимость со стандартными интерфейсами Java (`Executor`, `Future`)



