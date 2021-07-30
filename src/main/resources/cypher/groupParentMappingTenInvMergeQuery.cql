match p=(f:ent)-[r:teninvmerge]->(t:ent)
  where id(f)=$from and id(t)=$to
return r as link