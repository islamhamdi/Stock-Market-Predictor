Stock Market Activity Forecasting
===========================

Stock market prediction has been an active area of research for a long time. Besides the motivation of gaining an opportunity to invest in a market, predicting the price of a security using various statistical tools or any other techniques is still an ongoing field of research.

Stock market is driven by lots of dynamics in which facts and beliefs play a huge role in affecting the price and trading volume of a company's stock. In our model, we try to have an early indicator for price and volume movement in the market using microblogging services like Twitter.com and Stocktwits.com

We present a model in which we try to find a correlation between the public activity in microblogging services streams and stock market events.
As we've analyzed data from both sources for ~3 months, over 4 million tweets from both, and after representing the information into a constrained graph for features evaluation for some ML models, we had the following findings:

- Stocktwits has a strong correlation between a set of features (number of different users that have posted in G, number of nodes in G, number of tweets, number of connected components) and the trading volume, while in Twitter we found that the number of connected components has a correlation with the trading volume.
- Both Stocktwits and Twitter have weak correlation with price movement.
- Sentiment analysis does not improve our correlation results.
- Using various Machine Learning algorithms to find a better model for predicting price movements than Cross-Correlation coefficient, we found out that we could predict the future movement of a price with correctness over 65%

