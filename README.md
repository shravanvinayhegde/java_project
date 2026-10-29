# ATM Cash Dispenser with Denomination Logic

A console-based Java simulation of an ATM withdrawal workflow. It is based on the project report for **Object Oriented Programming using Java & Kotlin (4CTGC2011)**.

## Features

- Card/PIN authentication and card blocking after three failed attempts.
- Withdrawal validation (Rs.100–Rs.20,000, in multiples of Rs.10).
- Balance and daily-withdrawal-limit checks.
- Cash inventory tracked by denomination: Rs.500, Rs.200, Rs.100, Rs.50, Rs.20 and Rs.10.
- Interchangeable greedy and bounded dynamic-programming denomination strategies.
- Strategy Pattern, enums, records, generics, collections and checked custom exceptions.
- Thread-safe withdrawals with a fair `ReentrantLock` and synchronised account/inventory methods.
- Mini statement with successful and failed withdrawal attempts.
- Dependency-free tests for denomination selection, rollback behaviour, authentication and concurrent withdrawals.

> **Educational simulation only:** this project does not connect to a bank or real ATM hardware. The sample account, PIN, balances and notes are in memory and reset each time the app starts. The sample PIN is deliberately plain text for demonstration; never store PINs this way in a real financial application.

## Requirements

- JDK 17 or later (the project report was developed and tested on JDK 21).
- PowerShell on Windows, or a terminal with `javac` and `java` available.

Verify Java is installed:

```powershell
java -version
javac -version
```

## Run on Windows (PowerShell)

From the repository root:

```powershell
.\run.ps1
```

Demo credentials:

- Card number: `1234567890123456`
- PIN: `1234`

The application lets you withdraw cash, view a mini statement, check your balance and switch between the Greedy and Optimal strategies.

## Run manually (Windows PowerShell)

```powershell
$sources = Get-ChildItem -Path 'src/main/java' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
New-Item -ItemType Directory -Force -Path 'out' | Out-Null
javac -encoding UTF-8 -d out $sources
java -cp out com.atm.Main
```

## Run on Linux/macOS

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java -name '*.java')
java -cp out com.atm.Main
```

## Run tests

On Windows PowerShell:

```powershell
.\test.ps1
```

On Linux/macOS:

```bash
mkdir -p out-test
javac -encoding UTF-8 -d out-test $(find src/main/java src/test/java -name '*.java')
java -ea -cp out-test com.atm.ATMTest
```

The test runner checks that:

1. Rs.2,870 can be represented as five Rs.500 notes, one Rs.200, one Rs.100, one Rs.50 and one Rs.20.
2. Greedy can fail to make Rs.600 with two Rs.500 notes and three Rs.200 notes, while Optimal selects three Rs.200 notes.
3. Invalid and insufficient-funds withdrawals do not alter the account balance or inventory.
4. Five simultaneous Rs.1,000 withdrawals against a Rs.3,000 balance result in exactly three successes.
5. Three incorrect PIN attempts block the card.

## Source layout

```text
src/
├── main/java/com/atm/
│   ├── Account.java
│   ├── ATM.java
│   ├── ATMException.java
│   ├── AuthenticationException.java
│   ├── CashInventory.java
│   ├── DailyLimitExceededException.java
│   ├── Denomination.java
│   ├── DispenseStrategy.java
│   ├── GreedyDispenser.java
│   ├── InsufficientCashException.java
│   ├── InsufficientFundsException.java
│   ├── InvalidAmountException.java
│   ├── Main.java
│   ├── OptimalDispenser.java
│   └── Transaction.java
└── test/java/com/atm/ATMTest.java
```

``

## Concepts demonstrated

Encapsulation, abstraction, inheritance, polymorphism, Strategy Pattern, enums, records, generics, collections, checked exceptions, bounded dynamic programming, greedy selection and concurrency control.
