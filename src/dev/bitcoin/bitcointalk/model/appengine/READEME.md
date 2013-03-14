You may notice that every collection inside the App Engine model classes is repeated
twice. This is because one collection is a collection of keys, representing the objects,
and the other one is the actual list of objects itself. The list of keys is for App Engine/Objectify,
then once we actually want to send the object out the API we must load the objects for real
and assign them to the second field, so that the serializer can do its job. 