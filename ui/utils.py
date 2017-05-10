"""Utilities for soundwave ui server."""
import json
import logging
import urllib

import requests

from config import SOUNDWAVE_LOG_PATH, SOUNDWAVE_ACCESS_LOG, SOUNDWAVE_API


# Process multi level fields in query
def process_hierarchy(field, data):
    """Process dictionary hierarchy."""
    field_arr = field.split(".")
    data_set = data
    for field in field_arr:
        data_set = data_set[field]

    return str(data_set)


def get_soundwave_url(host, query, fields, onlyrunning):
    """Form Soundwave url."""
    url_dictionary = {
        "query": query,
        "fields": fields,
        "onlyrunning": onlyrunning,
    }

    url_str = host + "/results?" + urllib.urlencode(url_dictionary)
    return url_str


def get_url(id):
    """Gen url."""
    return "<a target='_blank' href='/instance/" + \
           str(id) + "'> " + str(id) + "</a>"


def json_load_byteified(file_handle):
    """JsonBytes."""
    return _byteify(
        json.load(file_handle, object_hook=_byteify),
        ignore_dicts=True)


def json_loads_byteified(json_text):
    """JsonLoads."""
    return _byteify(
        json.loads(json_text, object_hook=_byteify),
        ignore_dicts=False)


def _byteify(data, ignore_dicts=False):
    # if this is a unicode string, return its string representation
    if isinstance(data, unicode):
        return data.encode('utf-8')
    # if this is a list of values, return list of byteified values
    if isinstance(data, list):
        return [_byteify(item, ignore_dicts=True) for item in data]
    # if this is a dictionary, return dictionary of byteified keys and values
    # but only if we haven't already byteified it
    if isinstance(data, dict) and not ignore_dicts:
        return {
            _byteify(key, ignore_dicts=True): _byteify(value, ignore_dicts=True)
            for key, value in data.iteritems()}
    # if it's anything else, return it in its original form
    return data


def initialize_logger():
    """Log initializer."""
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)

    # create console handler and set level to info
    handler = logging.StreamHandler()
    handler.setLevel(logging.INFO)
    formatter = logging.Formatter("%(asctime)s - %(levelname)s - %(message)s")
    handler.setFormatter(formatter)
    logger.addHandler(handler)

    # create error file handler and set level to error
    handler = logging.FileHandler(SOUNDWAVE_LOG_PATH + SOUNDWAVE_ACCESS_LOG, "w", encoding="utf8", delay="true")
    handler.setLevel(logging.INFO)
    formatter = logging.Formatter("%(asctime)s - %(levelname)s - %(message)s")
    handler.setFormatter(formatter)
    logger.addHandler(handler)

    return logger


# get table data from response
def get_table_data(response, table_headers):
    """Generate table data format."""
    table_data = []
    for row in response.json():
        data_row = []
        for field in table_headers:
            if field in row:
                if field == "id":
                    data_row.append(get_url(row[field]))
                else:
                    data_row.append(str(row[field]))
            else:
                data_row.append("")

        table_data.append(data_row)

    return table_data


# get reservation rows
def get_reservation_rows(reservations):
    """Reservation display rows."""
    table_data = []
    for row in reservations:
        if "availability_zone" in row:
            region = str(row["availability_zone"])
        elif "region" in row:
            region = str(row["region"])

        data_row = [region, str(row["instance_type"]),
                    str(row["reserved_count"]), str(row["spot_count"]),
                    str(row["ondemand_count"]), str(row["unused_count"]),
                    str(row["active_count"])]
        table_data.append(data_row)

    return table_data


# query the soundwave backend via the API
def query_soundwave(query_str, fields):
    """Create Soundwave API query."""
    # Make a call to Soundwave endpoint
    query_str = query_str.lstrip().rstrip()

    # Replace all spaces with ""
    fields = fields.replace(" ", "")

    endpoint = SOUNDWAVE_API + "query"
    json_data = {"query": query_str, "fields": fields}

    response = requests.post(endpoint, json=json_data)

    return response
