# Refer https://docs.docker.com/engine/userguide/eng-image/dockerfile_best-practices/
# for best practices maintaining this file

# Build Java8 docker. Credit is to https://github.com/cogniteev/docker-oracle-java,
# which is licensed under the MIT license
# Pull base image.
FROM ubuntu:14.04

# Install Java.
RUN \
  apt-get update && \
  apt-get install -y software-properties-common && \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer


# Define working directory.
WORKDIR /data

# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle


# Create and set current directory
WORKDIR /opt/soundwave-worker

# Add the build artifact under /opt, can be overridden by docker build
ARG ARTIFACT_PATH=target/soundwave-worker-0.1-SNAPSHOT-bin.tar.gz
ADD $ARTIFACT_PATH /opt/soundwave-worker/

# Default command to run service, do not override it in docker run unless have a good reason
# Use "docker logs ID" to view stdout and stderr
CMD ["scripts/run_in_container.sh"]
