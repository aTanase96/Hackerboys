# Lab 4 - RabbitMQ Log Parser

## Emit Logs

Emit logs will take the input from the access.log file in the input folder and send the status code from the logs using RegEx.
RegEx is used here to make sure the status code that is sent only consists of 3 digit long numbers.

## Receive Logs

Receive logs will receive the messages sent by emit logs.
We have added an if statement and a log buffer. This will help us find any potential malicious activity from any ip addresses. This takes all the formatted logs and checks if the same ip address gets more then 10 of the same error code in a row then labels the log with malicious flag.

### The terminal output with the malicious flag.

![example 1](https://i.imgur.com/uQy4G73.png)

![example 2](https://i.imgur.com/JH0x25W.jpg)

![example 3](https://i.imgur.com/cKtWFO8.png)
