#! /bin/bash

python ~/workspace/engine/py-scripts/s4j/s4j.py --input scripts/db_api.api --out gen-java

python ~/workspace/api-kit/apikit.py --lang servlet --dir gen-java --context /api --ns gen.api --api scripts/rest_api.api
