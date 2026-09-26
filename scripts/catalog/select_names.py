"""Pick the most popular recent girl names that the app doesn't already have.

Input: SSA national baby-name counts as CSV (year,sex,name,n,prop). ssa.gov blocks scripted
downloads, so this uses the TidyTuesday mirror (data through 2017):
  https://raw.githubusercontent.com/rfordatascience/tidytuesday/master/data/2022/2022-03-22/babynames.csv

Usage: python3 select_names.py babynames.csv girls_1500.json [count]
"""
import collections
import csv
import glob
import json
import os
import re
import sys

SEED_DIR = os.path.join(os.path.dirname(__file__), "../../app/src/main/java/com/example/data/seed")

src, dest = sys.argv[1], sys.argv[2]
count = int(sys.argv[3]) if len(sys.argv) > 3 else 1500

existing = set()
for f in glob.glob(os.path.join(SEED_DIR, "Seed*.kt")):
    for m in re.finditer(r'^\s+[a-z]\("[^"]*",\s*"([^"]+)"', open(f).read(), re.M):
        existing.add(m.group(1).lower())

girls, boys = collections.Counter(), collections.Counter()
with open(src) as fh:
    for r in csv.DictReader(fh):
        if 2013 <= int(r["year"]) <= 2017:
            (girls if r["sex"] == "F" else boys)[r["name"]] += int(r["n"])

picked = []
for rank, (name, n) in enumerate(girls.most_common(), start=1):
    if name.lower() in existing:
        continue
    # Mark as unisex when more than a quarter of babies with the name are boys
    picked.append({"name": name, "rank": rank, "unisex": boys[name] > 0.25 * (n + boys[name])})
    if len(picked) == count:
        break

json.dump(picked, open(dest, "w"))
print(f"picked {len(picked)} names, ranks {picked[0]['rank']}-{picked[-1]['rank']}")
