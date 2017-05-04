#
# a scala content server
#
# https://github.com/marcolotz/ContentServer
#

# Pull base image
FROM  hseeberger/scala-sbt

#ENV SCALA_VERSION 2.12.0
#ENV SBT_VERSION 0.13.15

# Scala expects this file
#RUN touch /usr/lib/jvm/java-8-openjdk-amd64/release

# Install Scala
## Piping curl directly in tar
#RUN \
#  curl -fsL http://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
#  echo >> /root/.bashrc && \
#  echo 'export PATH=~/scala-$SCALA_VERSION/bin:$PATH' >> /root/.bashrc

# Install sbt
#RUN \
#  curl -L -o sbt-$SBT_VERSION.deb http://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
#  dpkg -i sbt-$SBT_VERSION.deb && \
#  rm sbt-$SBT_VERSION.deb && \
#  apt-get update && \
#  apt-get install sbt && \
#  sbt sbtVersion

# Install git
RUN \
  apt-get install git

# Install content server
  RUN \
    cd /root && \
    git clone https://github.com/marcolotz/contentserver

# Define working directory
WORKDIR /root