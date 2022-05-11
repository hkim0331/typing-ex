#!/bin/sh
if [ -z "$1" ]; then
    echo "usage: $0 <version>"
    exit
fi

SED="/bin/sed"
if [ -x "${HOMEBREW_PREFIX}/bin/gsed" ]; then
    SED=${HOMEBREW_PREFIX}/bin/gsed
fi

# package.json
${SED} -E -i "s/\"version\": .+$/\"version\": \"$1\",/" package.json

# project
${SED} -E -i "s/^\(defproject (.+) .+/(defproject \1 \"$1\"/" project.clj

# clj
${SED} -E -i \
    -e "s/^\(def \^:private version .+/(def ^:private version \"$1\")/" \
    src/typing_ex/view/page.clj

# cljs
${SED} -E -i \
    -e "s/^\(def \^:private version .+/(def ^:private version \"$1\")/" \
    -e "s/^\(def \^:private timeout .+/(def ^:private timeout 60)/" \
    src/typing_ex/typing.cljs
