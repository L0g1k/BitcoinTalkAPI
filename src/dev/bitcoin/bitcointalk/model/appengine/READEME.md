You may notice that every collection inside the App Engine model classes is repeated
twice. This is because one collection is a collection of keys, representing the objects,
and the other one is the actual list of objects itself. The list of keys is for App Engine/Objectify,
then once we actually want to send the object out the API we must load the objects for real
and assign them to the second field, so that the serializer can do its job. 

The other thing you might notice is that I don't use any inheritence in the classes. My opinion
of this is that it's better to violate DRY than use inheritence, because if you change
the inheritence structure at all, then all of the stored entities in the App Engine database have
to be migrated. That's a huge pain! Interfaces are ok though since they don't affect the stored entity.