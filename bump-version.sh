#!/bin/sh
if [ -z "$1" ]; then
    echo "usage: $0 <version>"
    exit
fi

SED="/bin/sed"
if [ -x "${HOMEBREW_PREFIX}/bin/gsed" ]; then
    SED=${HOMEBREW_PREFIX}/bin/gsed
fi

# project
${SED} -E -i "s/^\(defproject (.+) .+/(defproject \1 \"$1\"/" project.clj

# clj
${SED} -E -i "s/^\(def \^:private version .+/(def ^:private version \"$1\")/" src/typing_ex/view/page.clj

# cljs
${SED} -E -i "s/^\(def \^:private version .+/(def ^:private version \"$1\")/" src/typing_ex/typing.cljs
