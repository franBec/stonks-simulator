function fn() {
  var env = karate.env || 'local';
  var config = {
    baseUrl: 'http://localhost:8080'
  };

  if (env === 'staging') {
    config.baseUrl = 'https://stonks-staging.example.com';
  } else if (env === 'prod') {
    config.baseUrl = 'https://stonks.example.com';
  }

  karate.configure('connectTimeout', 5000);
  karate.configure('readTimeout', 30000);

  karate.log('karate env:', env, '| baseUrl:', config.baseUrl);
  return config;
}
