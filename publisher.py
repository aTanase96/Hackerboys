import paho.mqtt.client as mqtt

# This is the Publisher

# Called when the broker responds to our connection request.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    client.subscribe("mytopic")

# Called when a message has been received on a topic that the client
# subscribes to and the message does not match an existing topic filter callback.
def on_message(client, userdata, msg):
    if msg.payload.decode() == "Pong.":
        print("Message received (Pong.)")
        client.disconnect()

client = mqtt.Client()
client.connect("localhost",1883, 60)
client.publish("mytopic", "Ping.")

client.on_connect = on_connect
client.on_message = on_message

client.loop_forever()
