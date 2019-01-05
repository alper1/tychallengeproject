FROM openjdk:10-jdk

MAINTAINER akocabiyik

ENV OUTPUT_FILES_DIR="/"
COPY target/tychallenge-1.0-SNAPSHOT-allinone.jar tychallenge-1.0-SNAPSHOT-allinone.jar

WORKDIR /

COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod a+x /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
