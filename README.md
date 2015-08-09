# dvonnalyzer

Simple review tool for game of Dvonn.

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

Uses Reagent as React.js interface, Selmer for templates and figwheel
for live development.

## Running

To start a web server for the application, run:

    lein ring server

or to use automatic Clojure code reloading:

    lein ring server-headless

but the of all is just:

    lein figwheel

which runs webserver, compiles javascript on change and sends it to the
browser!

## License

Copyright © 2014 FIXME
