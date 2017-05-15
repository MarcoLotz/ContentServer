#
# a scala content server
#
# https://github.com/marcolotz/ContentServer
#

# Pull base image
FROM  hseeberger/scala-sbt

# Install git
RUN \
  apt-get install git

# Install content server
RUN \
  cd /root && \
  git clone https://github.com/marcolotz/contentserver

# Define working directory
WORKDIR /root
