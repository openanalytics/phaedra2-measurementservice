FROM registry.openanalytics.eu/proxy/library/eclipse-temurin:21-jre-noble

ARG JAR_FILE
ADD $JAR_FILE /opt/phaedra/service.jar

ENV USER phaedra
RUN useradd -c 'phaedra user' -m -d /home/$USER -s /bin/nologin $USER
WORKDIR /opt/phaedra
USER $USER

CMD ["java", "-jar", "-Xms2G", "-Xmx4G", "-XX:NewRatio=1", "-XX:SurvivorRatio=8", "-XX:+UseG1GC", "-XX:G1HeapRegionSize=16m", "-XX:MaxGCPauseMillis=200", "-XX:+UseStringDeduplication", "/opt/phaedra/service.jar", "--spring.jmx.enabled=false"]
