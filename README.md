# HKUST COMP 3111H Dieting Chatbot Project

## Links

Link to PostgreSQL Database: `heroku pg:psql postgresql-cylindrical-28609 --app dieting-chatbot`  
Link to Heroku repo: `https://git.heroku.com/dieting-chatbot.git` (push to this repo to deploy)  

## Test Coverage

**Testing coverage must be above 70% to get full marks.**

How to generate the test coverage report:

```
gradlew build
gradlew jacocoTestReport
```

Open the file `hkust_cs3111h\sample-spring-boot-kitchensink\build\jacocoHtml\index.html`

You can click into "com.example.bot.spring" to see which branch has not been covered and write a test for it.

The coverage for "missed branch" is the one we raise to 70%.

Delete codes that are not used to raise the test coverage percentage!!!!!!!!