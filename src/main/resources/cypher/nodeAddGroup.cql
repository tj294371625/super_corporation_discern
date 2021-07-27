match (e:ent {nodeid: $entId})
set e.groupid = $groupEntId
with collect({result: e}) as moreinfo
return moreinfo;