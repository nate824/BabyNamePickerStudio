# Name catalog

`app/src/main/assets/catalog_girls.json` is built in two steps:

1. `python3 select_names.py babynames.csv girls_1500.json 1500` picks the most popular girl names
   (US Social Security data, 2013–2017 births) that aren't already in the built-in seed lists.
2. `enrich.mjs` asks Claude (Message Batches API, half price) for each name's origin, meaning,
   pronunciation and style tags. It runs inside the server container so the API key stays there:

```bash
ssh root@187.124.147.150 'mkdir -p /opt/kindred/data/enrich'
scp enrich.mjs girls_1500.json root@187.124.147.150:/opt/kindred/data/enrich/
ssh root@187.124.147.150 'chown -R 1000:1000 /opt/kindred/data/enrich &&
  docker exec -u root kindred-sync cp /data/enrich/enrich.mjs /app/ &&
  docker exec -w /app kindred-sync node /app/enrich.mjs /data/enrich'
scp root@187.124.147.150:/opt/kindred/data/enrich/enriched.json ../../app/src/main/assets/catalog_girls.json
```

The script saves the batch id, so re-running it resumes polling instead of paying for a new batch.
Delete `/opt/kindred/data/enrich/batch_id` to start fresh.
