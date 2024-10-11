NUTS2JSON_API_BASE = "https://raw.githubusercontent.com/eurostat/Nuts2json/master/pub"

import argparse
import requests
import sys
import os

def get_nutsrg_geojson(nuts_level: int, year: int, projection: int, scale: str = "20M", version: str = "v2") -> str:
    return requests.get(f"{NUTS2JSON_API_BASE}/{version}/{year}/{projection}/{scale}/nutsrg_{nuts_level}.json").text


if __name__ == '__main__':
    ap = argparse.ArgumentParser(prog="NUTS GeoJSON Exporter",
                                 description="Aggregates EU NUTS-Regions from https://github.com/eurostat/Nuts2json")
    

    ap.add_argument("target_dir", type=str)
    ap.add_argument("--epsg", type=int, default=4326, choices=[3035, 3857, 4326], help="target projection (EPSG)", required=False)
    ap.add_argument("--year", type=int, default=2024, help="year the data was published (only selected years)", required=False)
    args = ap.parse_args()

    if not os.path.isdir(args.target_dir):
        os.makedirs(args.target_dir)
    
    # NUTS-Level 0 (National)
    with open(f"{args.target_dir}/nutsrg_0.geojson", 'w') as fp:
        fp.write(get_nutsrg_geojson(0, args.year, args.epsg))

    # NUTS-Level 1 (Major socio-economic regions)
    with open(f"{args.target_dir}/nutsrg_1.geojson", 'w') as fp:
        fp.write(get_nutsrg_geojson(1, args.year, args.epsg))
    
    # NUTS-Level 2 (Basic regions for the application of regional policies)
    with open(f"{args.target_dir}/nutsrg_2.geojson", 'w') as fp:
        fp.write(get_nutsrg_geojson(2, args.year, args.epsg))
    
    # NUTS-Level 3 (Basic regions for the application of regional policies)
    with open(f"{args.target_dir}/nutsrg_3.geojson", 'w') as fp:
        fp.write(get_nutsrg_geojson(3, args.year, args.epsg))
    
    print("Done.")