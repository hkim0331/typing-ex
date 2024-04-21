FROM clojure:lein

ENV DEBIAN_FRONTEND=noninteractive
ENV DEBCONF_NOWARNINGS=yes

RUN set -ex; \
    apt-get -y update; \
    apt-get -y upgrade; \
    apt-get -y install --no-install-recommends git npm postgresql-client

RUN apt-get -y autoremove && apt-get clean -y && rm -rf /var/lib/apt/lists/*

# ARG USERNAME=vscode
# ARG USER_UID=1000
# ARG USER_GID=$USER_UID
# RUN groupadd --gid $USER_GID $USERNAME \
#     && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME \
#     && echo ${USERNAME} ALL=\(ALL\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME \
#     && chmod 0440 /etc/sudoers.d/$USERNAME
# USER $USERNAME

WORKDIR /usr/src/app

# COPY package.json ./
# RUN npm install
# RUN npx shadow-cljs watch app

ENTRYPOINT [ "lein", "repl", ":headless" ]
