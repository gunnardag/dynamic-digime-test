### Digime test

###### To run this test make the following changes

1. In SplashActivity change << contract id >> to your contract id
2. In SplashActivity change << contract password >> to your contract password
3. In RequestHandler change << your computers ip >> to your computer's ip.
4. Add your p12 file in the python folder
5. In app.py in your pyhon folder change << contract id >> to the p12 file's name

6. navigate to your python folder and run the following commands 
```bash
export FLASK_APP = app.py
flask run --host=0.0.0.0
```

7. Run your app and click the **GET DATA** button.