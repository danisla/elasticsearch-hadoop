#FROM java:7
FROM elasticsearch-hadoop:base

WORKDIR /opt/app

RUN mkdir -p /opt/app

ADD . /opt/app/

RUN /opt/app/gradlew distZip
