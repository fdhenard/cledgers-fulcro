# cledgers-fulcro

## local stuff

### first time

- `$ npm install shadow-cljs react react-dom --save`

## local startup

- frontend
	- start shadow-cljs server

	        $ cd <repos>/cledgers-fulcro
	        $ npx shadow-cljs server

	- navigate to [localhost:9630](http://localhost:9630)
	- start watch
	- for frontend only -> navigate another tab to [localhost:8000](http://localhost:8000)
	- open cider repl
	    - `M-x cider-connect`
	    - host: `localhost`
	    - port: see shadow-cljs output. eg. `shadow-cljs - nREPL server started on port 50132`
	    - `shadow.user> (shadow/repl :main)`
	    - test repl?: `(js/alert "Hi")`
- backend
	- emacs
		- cider-jack-in
		- `> (start)`
- navigate another tab to [http://localhost:3000/index.html](http://localhost:3000/index.html)
