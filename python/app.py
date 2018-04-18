from flask import Flask, request, send_file
# set the project root directory as the static folder, you can set others.
app = Flask(__name__)

@app.route('/')
def root():
    return send_file('9sljFupcf2VRUh2SviksY7acmUcUcSDB.p12')

