FROM python:3-onbuild

# Create log directory
RUN mkdir -p /var/log/soundwave_ui

# expose the port
EXPOSE 8080

# launch the app
CMD [ "python", "./app.py" ]