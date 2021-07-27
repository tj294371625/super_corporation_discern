match (e:ent {nodeid: $entId})
with collect({node: e}) as moreinfo
return moreinfo