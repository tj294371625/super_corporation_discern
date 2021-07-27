match (e:ent {nodeid: $entId, entstatus: '1'})
with collect({source: e}) as moreinfo
return moreinfo