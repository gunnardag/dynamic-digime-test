from flask import Flask, request, send_file
# set the project root directory as the static folder, you can set others.
app = Flask(__name__)

@app.route('/')
def root():
    return send_file('<< contract id >>.p12')

