FROM clojure:lein

RUN apt-get update \
    && apt-get -y upgrade \
    && apt-get -y install --no-install-recommends \
           sudo git npm postgresql-client-14 2>&1

RUN npm install -g yarn

ARG USERNAME=vscode
ARG USER_UID=1000
ARG USER_GID=$USER_UID

RUN groupadd --gid $USER_GID $USERNAME \
    && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME \
    && echo ${USERNAME} ALL=\(ALL\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME \
    && chmod 0440 /etc/sudoers.d/$USERNAME

USER $USERNAME

WORKDIR /usr/src/app
COPY package.json ./
RUN npm install

ENTRYPOINT [ "lein", "repl", ":headless" ]
#ENTRYPOINT [ "sleep", "infinity" ]
