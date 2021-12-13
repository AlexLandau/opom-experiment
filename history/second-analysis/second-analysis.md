In the second analysis, we take the strats used in the first generation's outcome and
run more simulations on them -- in this case bringing the number of iterations per
matchup up from 30 to 100. Then we see how much the outcome differs with more accurate
statistics to build on.

Running 8 processes wide, the additional matchup simulations took about 3-4 hours.
Stats come from commit 05e31f90817eeb8b9654e077d4b693377aaddc3c

Cur strategy:
mewtwo_blizzard: 15.533%
lapras_bide: 13.441%
mewtwo_bodyslam: 11.973%
mewtwo_fireblast: 8.76%
chansey_bide: 7.973%
dewgong_toxic: 7.828%
rhydon_earthquake: 7.388%
mewtwo_psychic: 6.458%
mewtwo_thunderbolt: 6.196%
mewtwo_recover: 5.416%
rhydon_seismictoss: 3.541%
omastar_toxic: 1.61%
omastar_icebeam: 1.178%
moltres_fireblast: 1.128%
mewtwo_bide: 0.789%
cloyster_clamp: 0.617%
vaporeon_hydropump: 0.171%

This is actually fairly different from before...

mewtwo_blizzard:    13.555% -> 15.533%
mewtwo_fireblast:   12.248% -> 8.76%
mewtwo_bodyslam:     9.115% -> 11.973%
lapras_bide:         8.5%   -> 13.441%
mewtwo_thunderbolt:  8.411% -> 6.196%
mewtwo_recover:      7.142% -> 5.416%
mewtwo_bide:         6.087% -> 0.789%
cloyster_clamp:      5.21%  -> 0.617%
mewtwo_icebeam:      4.515% -> ----
kabutops_slash:      4.502% -> ----
chansey_seismictoss: 4.022% -> ----
dewgong_toxic:       3.125% -> 7.828%
chansey_bide:        3.054% -> 7.973%
rhydon_seismictoss:  2.987% -> 3.541%
rhydon_earthquake:   2.268% -> 7.388%
snorlax_bodyslam:    1.488% -> ----
chansey_rest:        1.32%  -> ----
mewtwo_rest:         0.923% -> ----
omastar_icebeam:     0.751% -> 1.178%
vaporeon_hydropump:  0.56%  -> 0.171%
mewtwo_psychic:      0.218% -> 6.458%
omastar_toxic:       ------ -> 1.61%
moltres_fireblast:   ------ -> 1.128%

Let's do another round of simulations, to bring in the two new values... (This took 20 minutes)
New commit: a64c7283ad249f33667b1de0e7d972136239f909
(Note that this doesn't change the relationship between those and other movesets in the NE, other
than each other, but might in turn bring in others)

Cur strategy:
mewtwo_blizzard: 15.346%
lapras_bide: 13.232%
mewtwo_bodyslam: 11.92%
mewtwo_fireblast: 8.841%
chansey_bide: 7.981%
dewgong_toxic: 7.866%
rhydon_earthquake: 7.373%
mewtwo_psychic: 6.396%
mewtwo_thunderbolt: 6.305%
mewtwo_recover: 5.406%
rhydon_seismictoss: 3.507%
omastar_toxic: 1.505%
omastar_icebeam: 1.371%
moltres_fireblast: 1.054%
mewtwo_bide: 1.045%
cloyster_clamp: 0.623%
vaporeon_hydropump: 0.229%
Looks like we're done here
Best pure-strat effectiveness is mewtwo_psychic with effectiveness 0.500000000000001
Ran computations in 332 ms

No real changes, though the percentages shifted a little.

So, here's the thing I want to try for the third round of analysis: Can we arrive at matchup results that
are exact rather than approximate?