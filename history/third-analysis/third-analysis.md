Without doing the full analytical approach to getting numbers, I'm instead focusing on getting the
overall start-to-finish process automated, hopefully in a way that allows this to be done on some Gen 2 stuff in a
reasonable amount of time (not to mention other variants like a ban-list-based ranking).

The first full run came up with an equilibrium somewhat different from the previous one
(notably omitting mewtwo_fireblast):

Cur strategy:
mewtwo_blizzard: 14.915%
mewtwo_bodyslam: 13.829%
mewtwo_psychic: 12.969%
chansey_bide: 9.937%
mewtwo_recover: 9.863%
lapras_icebeam: 6.949%
mewtwo_thunderbolt: 5.539%
kabutops_slash: 5.332%
lapras_bide: 5.033%
rhydon_earthquake: 4.449%
cloyster_rest: 4.245%
dewgong_toxic: 2.822%
golem_toxic: 2.387%
gengar_toxic: 1.506%
rhydon_dig: 0.224%

Best against this strategy:
- 0.4996916609932929: haunter_toxic
- 0.4996905590433071: mewtwo_icebeam
- 0.4996273448814643: gastly_toxic
- 0.4983138272528619: mewtwo_fireblast
- 0.4955705287345228: rhydon_toxic
- 0.4936000063853583: gengar_megadrain
- 0.4914297403161554: lapras_toxic
- 0.4857976729659891: lapras_rest
- 0.472575262513437: golem_dig
- 0.47244734641664454: mewtwo_seismictoss
  Final version of in-group:
    - chansey_bide
    - chansey_rest
    - chansey_seismictoss
    - cloyster_clamp
    - cloyster_rest
    - dewgong_icebeam
    - dewgong_toxic
    - gengar_toxic
    - golem_toxic
    - haunter_hypnosis
    - jynx_psychic
    - kabutops_slash
    - lapras_bide
    - lapras_blizzard
    - lapras_icebeam
    - mewtwo_blizzard
    - mewtwo_bodyslam
    - mewtwo_psychic
    - mewtwo_recover
    - mewtwo_thunderbolt
    - rhydon_dig
    - rhydon_earthquake
    - rhydon_toxic

What if I run this again without removing any of the existing stuff from the store of already-run games?

Cur strategy:
mewtwo_blizzard: 13.947%
mewtwo_bodyslam: 13.853%
mewtwo_psychic: 12.932%
mewtwo_recover: 9.724%
chansey_bide: 9.694%
lapras_icebeam: 6.473%
mewtwo_thunderbolt: 5.617%
lapras_bide: 5.321%
kabutops_slash: 5.279%
rhydon_earthquake: 4.621%
cloyster_rest: 4.251%
dewgong_toxic: 3.036%
golem_toxic: 2.401%
mewtwo_icebeam: 1.402%
gengar_toxic: 1.293%
rhydon_dig: 0.153%
mewtwo_fireblast: 0.001%

Best against this strategy:
- 0.49968742603477734: haunter_toxic
- 0.49962491124173275: gastly_toxic
- 0.49552068020450163: rhydon_toxic
- 0.4908077751387028: lapras_toxic
- 0.48442326340449016: lapras_rest
- 0.4777238235962182: gengar_megadrain
- 0.4730958041546928: golem_dig
- 0.47176501941684124: lapras_solarbeam
- 0.4708512326717303: lapras_blizzard
- 0.4707971135359749: rhydon_seismictoss

Final version of in-group:
- alakazam_psychic
- chansey_bide
- cloyster_rest
- dewgong_toxic
- dragonite_bodyslam
- gengar_megadrain
- gengar_toxic
- golem_toxic
- kabutops_slash
- lapras_bide
- lapras_icebeam
- lapras_rest
- lapras_toxic
- mewtwo_blizzard
- mewtwo_bodyslam
- mewtwo_fireblast
- mewtwo_icebeam
- mewtwo_psychic
- mewtwo_recover
- mewtwo_thunderbolt
- rhydon_dig
- rhydon_earthquake
- rhydon_toxic
- snorlax_bodyslam

And then after adding in-group persistence:

mewtwo_blizzard: 17.112%
mewtwo_bodyslam: 13.538%
mewtwo_psychic: 10.915%
mewtwo_recover: 8.541%
lapras_bide: 7.454%
chansey_bide: 7.125%
mewtwo_thunderbolt: 7.002%
lapras_icebeam: 6.008%
kabutops_slash: 5.646%
rhydon_earthquake: 4.562%
dewgong_toxic: 3.543%
golem_toxic: 2.535%
cloyster_rest: 2.475%
mewtwo_fireblast: 2.257%
rhydon_dig: 1.288%
Strategy members / in-group members: 15/31
Boosting against-strategy stats to 5...
Best against this strategy:
- 0.49732710566331584: mewtwo_icebeam
- 0.4943635764624877: rhydon_toxic
- 0.4894481095587875: lapras_toxic
- 0.4892747783821168: lapras_rest
- 0.4845230671860113: gengar_toxic
- 0.4838819844711903: haunter_toxic
- 0.4838393722517882: gastly_toxic
- 0.478734331193997: lapras_blizzard
- 0.4736819617217321: cloyster_clamp
- 0.4733209192152512: snorlax_bodyslam
Maybe ran out of new entries? Ending for now
Final version of in-group:
- articuno_blizzard
- chansey_bide
- cloyster_rest
- dewgong_toxic
- dodrio_bodyslam
- electabuzz_thunderbolt
- gastly_toxic
- gengar_megadrain
- golem_toxic
- haunter_thunderbolt
- kabutops_slash
- lapras_bide
- lapras_blizzard
- lapras_icebeam
- lapras_rest
- lapras_toxic
- magneton_thunderbolt
- mewtwo_bide
- mewtwo_blizzard
- mewtwo_bodyslam
- mewtwo_fireblast
- mewtwo_megakick
- mewtwo_psychic
- mewtwo_recover
- mewtwo_seismictoss
- mewtwo_thunderbolt
- rhydon_dig
- rhydon_earthquake
- rhydon_seismictoss
- rhydon_toxic
- vaporeon_surf

... which is stable on rerunning (in-group sample size: 410)

Now, bumping up the against-group sample size to 20 and the within-group sample size to 1000:

Cur strategy:
mewtwo_blizzard: 17.857%
mewtwo_bodyslam: 13.536%
mewtwo_psychic: 11.464%
lapras_bide: 8.308%
mewtwo_recover: 8.262%
chansey_bide: 6.706%
mewtwo_thunderbolt: 6.627%
kabutops_slash: 5.706%
lapras_icebeam: 4.695%
rhydon_earthquake: 4.041%
dewgong_toxic: 3.703%
golem_toxic: 2.903%
mewtwo_fireblast: 2.745%
cloyster_rest: 2.514%
rhydon_dig: 0.934%
Strategy members / in-group members: 15/22
Boosting against-strategy stats to 20...
Collected statistics in 1016.986 seconds
Loaded results in 0.567 seconds
Best against this strategy:
- 0.4972766590636336: mewtwo_icebeam
- 0.49246365638394535: rhydon_toxic
- 0.49165790083319694: gengar_toxic
- 0.4914290623747417: haunter_toxic
- 0.49126336366984324: gastly_toxic
- 0.48914036222847335: omastar_seismictoss
- 0.48873981387609206: lapras_toxic
- 0.4872496577384175: lapras_rest
- 0.47891553313522073: lapras_blizzard
- 0.4768684174322592: snorlax_bodyslam
Final version of in-group:
- chansey_bide
- cloyster_rest
- dewgong_toxic
- gastly_toxic
- gengar_megadrain
- golem_toxic
- kabutops_slash
- lapras_bide
- lapras_blizzard
- lapras_icebeam
- lapras_rest
- lapras_toxic
- mewtwo_blizzard
- mewtwo_bodyslam
- mewtwo_fireblast
- mewtwo_psychic
- mewtwo_recover
- mewtwo_thunderbolt
- rhydon_dig
- rhydon_earthquake
- rhydon_seismictoss
- rhydon_toxic


Next I added some stuff to bring us up to a certain level of statistical confidence that we aren't
missing anything in the "outside" group that should actually be in the in-group. This involves statistical
tests for each regarding whether the actual value might be >= 0.5, as well as a code path that boosts sample
sizes to get the sum of such p-values down below 0.05.

Cur strategy:
mewtwo_blizzard: 17.065%
mewtwo_bodyslam: 12.913%
mewtwo_psychic: 11.145%
mewtwo_recover: 8.286%
mewtwo_thunderbolt: 7.703%
lapras_bide: 7.281%
chansey_bide: 7.025%
lapras_icebeam: 6.272%
kabutops_slash: 5.894%
rhydon_earthquake: 3.818%
dewgong_toxic: 3.295%
golem_toxic: 3.065%
mewtwo_fireblast: 2.978%
cloyster_rest: 2.338%
rhydon_dig: 0.921%
Strategy members / in-group members: 15/23
Best against this strategy:
  Best against this strategy:
- 0.4972805982867194: mewtwo_icebeam* (p=0.335)
- 0.49116821760371354: rhydon_toxic* (p=0.077)
- 0.4907028098890314: lapras_toxic* (p=0.067)
- 0.488700167066102: lapras_rest* (p=0.033)
- 0.4794230119992228: lapras_blizzard* (p=0)
- 0.4779629128668132: gengar_toxic (p=0)
- 0.47779418744796526: haunter_toxic (p=0)
- 0.477641707958012: gastly_toxic* (p=0)
- 0.47597429472756614: golem_dig (p=0.005)
- 0.47371667831468517: omastar_seismictoss (p=0.002)
Final version of in-group:
- chansey_bide
- cloyster_rest
- dewgong_toxic
- gastly_toxic
- gengar_megadrain
- golem_toxic
- kabutops_slash
- lapras_bide
- lapras_blizzard
- lapras_icebeam
- lapras_rest
- lapras_toxic
- mewtwo_blizzard
- mewtwo_bodyslam
- mewtwo_fireblast
- mewtwo_icebeam
- mewtwo_psychic
- mewtwo_recover
- mewtwo_thunderbolt
- rhydon_dig
- rhydon_earthquake
- rhydon_seismictoss
- rhydon_toxic
Sum of probabilities of unincluded being above 0.5: 0.04065336848064727

The next round should tackle the question of how accurate the numbers within the strategy are, which will probably be
harder...

I threw something together that feels decent -- it checks how much changing each matchup to its 95% confidence interval
bounds changes the strategy (measuring via the L1 distance) and repeatedly adds matchup runs to the most sensitive
matchups.

This gives us:

Cur strategy:
mewtwo_blizzard: 17.665%
mewtwo_bodyslam: 12.926%
mewtwo_psychic: 10.713%
lapras_bide: 8.369%
mewtwo_recover: 7.976%
mewtwo_thunderbolt: 7.719%
chansey_bide: 6.189%
kabutops_slash: 6.004%
lapras_icebeam: 5.757%
dewgong_toxic: 3.656%
rhydon_earthquake: 3.639%
mewtwo_fireblast: 3.416%
golem_toxic: 2.83%
cloyster_rest: 1.846%
rhydon_dig: 1.293%
Strategy members / in-group members: 15/22
Boosting against-strategy stats to 20...
Collected statistics in 3.384 seconds
Loaded results in 0.356 seconds
Best against this strategy:
- 0.4929746027505705: mewtwo_icebeam* (p=0.075)
- 0.49206060347986547: rhydon_toxic* (p=0.105)
- 0.4908028602021138: lapras_rest* (p=0.074)
- 0.489461192022825: lapras_toxic* (p=0.047)
- 0.47984654761210166: gengar_toxic (p=0.001)
- 0.47980850412494686: lapras_blizzard* (p=0.001)
- 0.47967689086702625: haunter_toxic (p=0.001)
- 0.4795565285185321: gastly_toxic* (p=0.001)
- 0.47300073497802253: golem_dig (p=0)
- 0.4725446373530581: snorlax_bodyslam (p=0)
  Collected statistics in 3.286 seconds
  Loaded results in 0.441 seconds
  Best against this strategy:
- 0.4929746027505705: mewtwo_icebeam* (p=0.075)
- 0.49206060347986547: rhydon_toxic* (p=0.105)
- 0.4908028602021138: lapras_rest* (p=0.074)
- 0.489461192022825: lapras_toxic* (p=0.047)
- 0.47984654761210166: gengar_toxic (p=0.001)
- 0.47980850412494686: lapras_blizzard* (p=0.001)
- 0.47967689086702625: haunter_toxic (p=0.001)
- 0.4795565285185321: gastly_toxic* (p=0.001)
- 0.47300073497802253: golem_dig (p=0)
- 0.4725446373530581: snorlax_bodyslam (p=0)
  Maybe ran out of new entries? Ending for now
  Final version of in-group:
- chansey_bide
- cloyster_rest
- dewgong_toxic
- gastly_toxic
- golem_toxic
- kabutops_slash
- lapras_bide
- lapras_blizzard
- lapras_icebeam
- lapras_rest
- lapras_toxic
- mewtwo_blizzard
- mewtwo_bodyslam
- mewtwo_fireblast
- mewtwo_icebeam
- mewtwo_psychic
- mewtwo_recover
- mewtwo_thunderbolt
- rhydon_dig
- rhydon_earthquake
- rhydon_seismictoss
- rhydon_toxic
  Sum of sensitivies: 0.9431912376148668
  Most sensitive matchups:
- 0.024924584520573757: lapras_icebeam vs. mewtwo_bodyslam
- 0.02104965252964305: mewtwo_blizzard vs. mewtwo_bodyslam
- 0.020935891214635904: chansey_bide vs. lapras_bide
- 0.02090427507470681: lapras_bide vs. mewtwo_blizzard
- 0.020834039818555153: mewtwo_blizzard vs. mewtwo_psychic
- 0.020756390762136444: lapras_icebeam vs. mewtwo_psychic
- 0.020740480832768533: chansey_bide vs. mewtwo_blizzard
- 0.02072551438099174: mewtwo_blizzard vs. mewtwo_thunderbolt
- 0.020679784881049813: lapras_icebeam vs. mewtwo_thunderbolt
- 0.02060293649293065: lapras_bide vs. mewtwo_bodyslam
  Sum of probabilities of unincluded being above 0.5: 0.0429279207884421

Now I just want to run this again from a fresh slate of data and see if we end up somewhere close by.

("Boosting against-strategy stats to 20..." takes quite a long time, 39 minutes, wonder if it's slightly overkill...)

Strat just before boosting sensitivities:

mewtwo_blizzard: 14.324%
mewtwo_bodyslam: 12.592%
mewtwo_psychic: 11.448%
lapras_icebeam: 9.398%
mewtwo_recover: 8.599%
mewtwo_thunderbolt: 8.538%
chansey_bide: 8.076%
kabutops_slash: 6.003%
lapras_bide: 5.412%
rhydon_earthquake: 4.165%
golem_toxic: 3.463%
cloyster_rest: 2.706%
dewgong_toxic: 2.55%
mewtwo_fireblast: 2.076%
rhydon_dig: 0.651%

And the final result:

Cur strategy:
mewtwo_blizzard: 15.073%
mewtwo_bodyslam: 12.398%
mewtwo_psychic: 11.877%
mewtwo_recover: 8.698%
lapras_icebeam: 8.327%
mewtwo_thunderbolt: 8.112%
chansey_bide: 8.051%
kabutops_slash: 6.41%
lapras_bide: 5.554%
golem_toxic: 3.541%
rhydon_earthquake: 3.345%
cloyster_rest: 2.805%
dewgong_toxic: 2.634%
mewtwo_fireblast: 2.266%
rhydon_dig: 0.909%
Strategy members / in-group members: 15/19
Boosting against-strategy stats to 20...
Best against this strategy:
- 0.4946982907386191: mewtwo_icebeam* (p=0.198)
- 0.49265611833871004: rhydon_toxic* (p=0.122)
- 0.49228342928572116: lapras_toxic* (p=0.107)
- 0.49158423132899337: lapras_rest* (p=0.09)
- 0.4790808996840018: golem_dig (p=0)
- 0.4731893006500011: haunter_toxic (p=0.003)
- 0.4731842348064525: gengar_toxic (p=0.003)
- 0.47312252125927234: gastly_toxic (p=0.003)
- 0.4721094814464308: lapras_blizzard (p=0.002)
- 0.4689329556469392: rhydon_seismictoss (p=0)
Best against this strategy:
- 0.4946982907386191: mewtwo_icebeam* (p=0.198)
- 0.49265611833871004: rhydon_toxic* (p=0.122)
- 0.49228342928572116: lapras_toxic* (p=0.107)
- 0.49158423132899337: lapras_rest* (p=0.09)
- 0.4790808996840018: golem_dig (p=0)
- 0.4731893006500011: haunter_toxic (p=0.003)
- 0.4731842348064525: gengar_toxic (p=0.003)
- 0.47312252125927234: gastly_toxic (p=0.003)
- 0.4721094814464308: lapras_blizzard (p=0.002)
- 0.4689329556469392: rhydon_seismictoss (p=0)
Maybe ran out of new entries? Ending for now
Final version of in-group:
- chansey_bide
- cloyster_rest
- dewgong_toxic
- golem_toxic
- kabutops_slash
- lapras_bide
- lapras_icebeam
- lapras_rest
- lapras_toxic
- mewtwo_blizzard
- mewtwo_bodyslam
- mewtwo_fireblast
- mewtwo_icebeam
- mewtwo_psychic
- mewtwo_recover
- mewtwo_thunderbolt
- rhydon_dig
- rhydon_earthquake
- rhydon_toxic
Sum of sensitivies: 0.9907273978989315
Most sensitive matchups:
- 0.028076578872012294: lapras_icebeam vs. mewtwo_bodyslam
- 0.02342416951114968: mewtwo_fireblast vs. mewtwo_psychic
- 0.023246047851938283: lapras_icebeam vs. mewtwo_psychic
- 0.022818379223594247: mewtwo_blizzard vs. mewtwo_psychic
- 0.02276275435398991: lapras_icebeam vs. mewtwo_recover
- 0.022754824469195096: mewtwo_blizzard vs. mewtwo_bodyslam
- 0.02267971998222252: lapras_bide vs. mewtwo_psychic
- 0.022588837678521375: mewtwo_blizzard vs. mewtwo_recover
- 0.02255314972833415: chansey_bide vs. mewtwo_bodyslam
- 0.02255147656746107: chansey_bide vs. lapras_icebeam
Sum of probabilities of unincluded being above 0.5: 0.04587625307365539
Time running whole analysis (if there wasn't a break in the middle): 10392.672 seconds (2.89 hours)



Comparing the two independent runs:

mewtwo_blizzard:   17.665% -> 15.073%
mewtwo_bodyslam:   12.926% -> 12.398%
mewtwo_psychic:    10.713% -> 11.877%
lapras_bide:        8.369% ->  5.554%
mewtwo_recover:     7.976% ->  8.698%
mewtwo_thunderbolt: 7.719% ->  8.112%
chansey_bide:       6.189% ->  8.051%
kabutops_slash:     6.004% ->  6.41%
lapras_icebeam:     5.757% ->  8.327%
dewgong_toxic:      3.656% ->  2.634%
rhydon_earthquake:  3.639% ->  3.345%
mewtwo_fireblast:   3.416% ->  2.266%
golem_toxic:        2.830% ->  3.541%
cloyster_rest:      1.846% ->  2.805%
rhydon_dig:         1.293% ->  0.909%

This is an L1 difference of 0.17572 and an L2 difference of 0.05587034991836.
