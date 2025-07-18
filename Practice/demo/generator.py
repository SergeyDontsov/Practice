import random
import json

names = [
    "spark", "hadoop", "flink", "storm", "cassandra",
    "pyspark", "elasticsearch", "druid", "clickhouse",
    "pinot", "scylla", "influxdb", "prometheus",
    "graylog", "sentry", "datanode", "namenode",
    "zeppelin", "airflow", "sqoop", "nifi", "kafka", 
    "zookeeper", "fluentd"
]

def generate_data(num_entries):
    data = []
    for _ in range(num_entries):
        entry = {
            "number": random.randint(1, 50000),
            "name": random.choice(names)
        }
        data.append(entry)
    return data

# Генерация данных
data_set = generate_data(1000000)

# Запись данных в файл
with open("nabor(group).json", "w") as f:
    json.dump(data_set, f, indent=4)

print("Данные успешно сохранены в output.json")