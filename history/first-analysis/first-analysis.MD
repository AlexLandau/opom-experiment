Trying to figure out how to get this equilibrium effectively...

Stats from Pokemon-Showdown: Come from f6a8372aebebe703eae9ef27d9be201e405bea32,
sample size 30

Related files:
analysis-1-plots.html
time-series-1
time-series-1-data.json

NaiveEquilibriumSearch: Too slow

CachingEquilibriumSearch: Good start, but then the rate of improvement starts to peter out...
at 300K samples, best pure strat against the chosen strat is 0.0015 effectiveness, but at 1M
only down to 0.0010

Conclusions:

1) Probably want to replace strategy with something that can use floating-point values
2) Try to find/try more algorithms for strategy iteration:
   - https://www.aaai.org/Papers/AAAI/2004/AAAI04-105.pdf
   - Govindan-Wilson (or Blum-Shelton-Koller)
   - https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.513.6235&rep=rep1&type=pdf
   - Can we just use linear programming?

I did (1) and used linear programming -- turns out this is a recommended way to solve Nash
Equilibria when the game is zero-sum, and I just didn't uncover that sooner. I'm using Apache
Commons Math to do the computations, which is adequate for my use case.

This appears to be THE optimal strategy for our current set of data:

Cur strategy:
mewtwo_blizzard: 13.555%
mewtwo_fireblast: 12.248%
mewtwo_bodyslam: 9.115%
lapras_bide: 8.5%
mewtwo_thunderbolt: 8.411%
mewtwo_recover: 7.142%
mewtwo_bide: 6.087%
cloyster_clamp: 5.21%
mewtwo_icebeam: 4.515%
kabutops_slash: 4.502%
chansey_seismictoss: 4.022%
dewgong_toxic: 3.125%
chansey_bide: 3.054%
rhydon_seismictoss: 2.987%
rhydon_earthquake: 2.268%
snorlax_bodyslam: 1.488%
chansey_rest: 1.32%
mewtwo_rest: 0.923%
omastar_icebeam: 0.751%
vaporeon_hydropump: 0.56%
mewtwo_psychic: 0.218%

Best pure-strat effectiveness is snorlax_bodyslam with effectiveness 0.5000000000000084.

And this is computable in half a second from an empty starting point (using my approach of iteratively
adding new pure strategies to the optimizer problem, one at a time). On the other hand, if I shove
everything in at once, the optimizer hangs -- going to try to see how long it takes, starting around
1 PM... I wonder if, if I used a better optimizer, it would emulate my optimization for me?

If I cram everything in there, it takes 398 seconds (or 6 minutes, 38 seconds).

This brings us to the end of the first analysis, based on the first set of data.
