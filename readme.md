# Modelli approssimanti
I modelli approssimanti che utilizziamo sono approssimanti di Whitt.
## Modello hyperexp
## Modello exp
## Modello hypoexp
### CV^2 >= 0.5
### CV^2 < 0.5

# Esperimenti

## Esperimenti fatti

## Esperimenti da fare
[ ] Confrontare JSD media as-time-passes al variare del numero di servers e di clients.

[ ] Siano X1 e X2 due stime consecutive del remaining time del tagged customer. Calcolare la distanza tra _X1 | t > t2_ e _X2_ e osservare se all'aumentare del numero degli eventi osservati la distanza diminuisce. 

## Confronto tra JSD medie al variare del numero di clients e servers
Siano _S = {1, 2, 4, 8}_ i servers, _C = {8, 16, 32, 64}_ i clients e T gli istanti di tempo a cui vengono rilevati gli eventi. Per ogni tupla _(s, c, t), s ∈ S, c ∈ C_, t ∈ T calcoliamo la Jensen-Shannon Divergence media tra il transiente del modello vero (nascosto) e quello approssimato (l'approssimazione tiene conto dei primi _t_ eventi).

Siccome l'approssimazione è fatta considerando una sequenza di eventi campionata dalla distribuzione del modello vero, campioniamo più volte da tale distribuzione, calcoliamo l'approssimazione e facciamo la media delle JSD risultanti. 

## Confronto tra approssimazioni successive
Siano _Xi_ e _Xj_ approssimazioni successive del remaining time del tagged customer basate rispettivamente sugli eventi osservati prima dell'istante _ti_ e dell'istante _tj_, _ti <= tj_.

Vogliamo calcolare la distanza che è presente tra _Xi | t > tj_ e _Xj_. [da finire]
