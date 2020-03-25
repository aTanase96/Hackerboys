import pika

# Run RabbitMQ:
# docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

connection = pika.BlockingConnection(pika.ConnectionParameters(host="localhost"))
channel = connection.channel()

channel.exchange_declare(exchange="direct_logs", exchange_type="direct")

logs = []
codes = set()

with open("input/access.log") as file:
    for line in file.readlines():
        logs.append(line.strip())

for log in logs:
    status_code = log.split(" ")[8]
    if status_code != "\"-\"" or status_code != "166":
        codes.add(status_code)
    channel.basic_publish(exchange="direct_logs", routing_key=status_code, body=log.encode())
    print(f"Sent log with status code {status_code}")

# print(codes)
connection.close()
