match p=(f:ent)-[r:teninvmerge]->(t:ent)
  where id(f)=$from and id(t)=$to
  and (date(r.start_time) <= date($hitTime) and date($hitTime) <= date(r.end_time))
return r as link