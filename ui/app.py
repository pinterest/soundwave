#!/usr/bin/env python
"""Web / UI Server for Soundwave"""
import json
import os
from datetime import datetime

import cherrypy
import cherrypy.wsgiserver
import requests
from flask import Flask, request, render_template, g, send_from_directory

from config import SOUNDWAVE_API, SOUNDWAVE_HOST, SOUNDWAVE_PORT
from utils import get_soundwave_url, json_loads_byteified, \
    initialize_logger, get_table_data, query_soundwave, get_reservation_rows

# Initializations
logger = initialize_logger()

app = Flask(__name__,
            template_folder='templates',
            static_folder='static')


@app.before_request
def log_request():
    """Request logger."""
    if "static" not in request.path:
        logger.info("\npath: %s \nhttp-method: %s \nheaders: %s",
                    str(request.path),
                    str(request.method),
                    str(request.headers))

        g.start = datetime.now()


@app.teardown_request
def teardown_request(exception=None):
    """Request timer."""
    if "static" not in request.path:
        response_time = datetime.now() - g.start
        logger.info("%s %s %s %s ms \n ------------------------",
                    str(request.host),
                    str(request.path),
                    str(request.method),
                    str(response_time.microseconds / 1000))


@app.route('/favicon.ico')
def favicon():
    """Favicon."""
    return send_from_directory(os.path.join(app.root_path, 'static'),
                               'favicon.ico', mimetype='image/vnd.microsoft.icon')


# Route for all 404 errors
@app.errorhandler(404)
def page_not_found(e):
    """Route to handle 404."""
    logger.warn(e)
    return render_template("404.html")


@app.errorhandler(500)
def server_error(e):
    """Route for all 500 errors."""
    logger.warn(e)
    return render_template("500.html")


# Route for the home page
@app.route("/", methods=["GET", "POST"])
def index():
    """Homepage."""
    if request.method == "GET":
        return render_template(
            "index.html",
            url_str="",
            query="",
            fields="id,state,config.name,config.internal_address",
            table_headers=[],
            table_data=[[]])

    if request.method == "POST":

        try:
            query = request.form.get("query")
            fields = request.form.get("fields")
            running_only = request.form.get("onlyrunning")

            # If only running instances are selected then edit query and add mandatory condition
            backend_query = query
            if running_only == "true":
                backend_query += " AND state:running"

            response = query_soundwave(backend_query, fields)

            if response.status_code == 200 or response.status_code == 304:

                table_headers = fields.split(",")
                table_data = get_table_data(response, table_headers)
                url_str = get_soundwave_url(request.host, query, fields, running_only)
                logger.info("Successfully returned query data")

                return render_template(
                    "index.html",
                    url_str=url_str,
                    query=query,
                    fields=fields,
                    table_headers=table_headers,
                    table_data=table_data)

            elif response.status_code == 404 or response.status_code == 400:
                logger.warn("Data not found in soundwave elastic search store. API returned 404")
                return render_template("404.html")

            elif response.status_code == 500:
                logger.warn("soundwave api returned 500 status code. Internal Server error")
                return render_template("500.html")

        except Exception as e:

            logger.warn(e.message)

            return render_template(
                "index.html",
                url_str="",
                error="Internal Server Error")


@app.route("/results", methods=["GET"])
def get_results():
    """Query Results page."""
    try:
        url_params = request.values
        query = str(url_params.get("query"))
        fields = str(url_params.get("fields"))
        running_only = str(url_params.get("onlyrunning"))

        # If only running instances are selected then edit query and add mandatory condition
        backend_query = query
        if running_only == "true":
            backend_query += " AND state:running"

        response = query_soundwave(backend_query, fields)

        if response.status_code == 200 or response.status_code == 304:

            table_headers = fields.split(",")
            table_data = get_table_data(response, table_headers)
            url_str = get_soundwave_url(request.host, query, fields, running_only)
            logger.info("Successfully returned query data")

            return render_template(
                "index.html",
                url_str=url_str,
                query=query,
                fields=fields,
                table_headers=table_headers,
                table_data=table_data)

        elif response.status_code == 404 or response.status_code == 400:
            logger.warn("Data not found in soundwave elastic search store. API returned 404")
            return render_template("404.html")

        elif response.status_code == 500:
            logger.warn("soundwave api returned 500 status code. Internal Server error")
            return render_template("500.html")

    except Exception as e:

        logger.warn(e.message)

        return render_template(
            "index.html",
            url_str="",
            error="Internal Server Error")


# Route for instance
@app.route("/instance/<instance_id>", methods=["GET"])
def get_instance(instance_id):
    """Instance information."""
    endpoint = SOUNDWAVE_API + "instance/" + instance_id
    logger.info("Get data for endpoint : " + endpoint)

    response = requests.get(endpoint)
    if response.status_code == 200 or response.status_code == 304:
        json_data = json_loads_byteified(response.text)

        return render_template(
            "instance.html",
            data=json.dumps(json_data),
            id=json_data["id"])

    elif response.status_code == 404 or response.status_code == 400:
        logger.warn("Data not found in soundwave elastic search store. API returned 404")
        return render_template("404.html")

    elif response.status_code == 500:
        logger.warn("soundwave api returned 500 status code. Internal Server error")
        return render_template("500.html")


# Route to check the application health from elb
@app.route("/_/_/", methods=["GET"])
def health_check():
    """Health check."""
    return "healthy"


# Route to get InstanceCountRecords
@app.route("/reservations", methods=["GET", "POST"])
def reservations():
    """Reservations."""
    if request.method == "GET":
        return render_template("reservations.html")

    elif request.method == "POST":

        date = request.form.get("date")
        endpoint = "instancetyperecords/daily/{0}".format(str(date))
        query = SOUNDWAVE_API + endpoint

        response = requests.get(query)
        if response.status_code == 200 or response.status_code == 304:
            reservations = response.json()
            table_headers = ["region", "instance type", "reserved", "spot", "on demand", "unused", "active"]
            table_data = get_reservation_rows(reservations)

            return render_template("reservations.html",
                                   table_headers=table_headers,
                                   table_data=table_data)

        elif response.status_code == 404 or response.status_code == 400:
            logger.warn("Data not found in soundwave elastic search store. API returned 404")
            return render_template("404.html")

        elif response.status_code == 500:
            logger.warn("soundwave api returned 500 status code. Internal Server error")
            return render_template("500.html")


@app.route("/aggregations", methods=["GET", "POST"])
def aggregations():
    """String aggregations on soundwave data."""
    if request.method == "GET":
        return render_template("aggregations.html",
                               query="",
                               data="")

    elif request.method == "POST":
        query = request.form.get("query")
        return aggregations_terms(query)


@app.route("/aggregations/terms/<query>", methods=["GET", "POST"])
def aggregations_terms(query=None):
    """Get page for aggregations."""
    if query is None:
        # Default query
        query = "state,config.instance_type"

    # Remove all white spaces from the str
    query = query.replace(" ", "")

    data = {"query": query}
    end_point = "aggregations/terms"
    url = SOUNDWAVE_API + end_point

    response = requests.post(url, json=data)
    if response.status_code == 200 or response.status_code == 304:
        json_data = json_loads_byteified(response.text)

        return render_template(
            "aggregations.html",
            data=json.dumps(json_data),
            query=query)

    elif response.status_code == 404 or response.status_code == 400:
        logger.warn("Data not found in soundwave elastic search store. API returned 404")
        return render_template("404.html")

    elif response.status_code == 500:
        logger.warn("soundwave api returned 500 status code. Internal Server error")
        return render_template("500.html")


def main():
    """Wsgi start function."""
    # Create a wsgi server
    wsgi_server = cherrypy.wsgiserver.WSGIPathInfoDispatcher({'/': app})
    cherrypy.tree.mount(None, '/static', config={
        '/': {
            'tools.caching.on': True,
            'tools.staticdir.on': True,
            'tools.staticdir.dir': app.static_folder
        },
    })
    server = cherrypy.wsgiserver.CherryPyWSGIServer((SOUNDWAVE_HOST, SOUNDWAVE_PORT), wsgi_server, numthreads=50)

    try:
        logger.info("Soundwave UI - Starting cherrypy wsgi server")
        server.start()

    except KeyboardInterrupt:
        server.stop()

    except Exception as e:
        logger.error(e)


if __name__ == "__main__":
    main()
