import os
import re

import pika

# Run RabbitMQ:
# docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

connection = pika.BlockingConnection(pika.ConnectionParameters(host="localhost"))
channel = connection.channel()

channel.exchange_declare(exchange="direct_logs", exchange_type="direct")

logs = []
codes = set()


# Open all files under '/input' and append them to 'logs'.
for root, dirs, files in os.walk('input/', topdown=False):
    for filename in files:
        print(os.path.join(root, filename))
        with open(os.path.join(root, filename), 'r') as f:
            for line in f.readlines():
                logs.append(line.strip())


for log in logs:
    status_code = re.search(r'\s([0-9]){3}\s', log).group(0).strip()
    channel.basic_publish(exchange="direct_logs", routing_key=status_code, body=log.encode())
    print(f"Sent log with status code {status_code}")

connection.close()
