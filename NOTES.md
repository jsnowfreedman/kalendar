# Series Table vs Events containing their own series information

Advantages of Series Table:
- Can have extra metadata for the series (like its own description/recurring/etc)
- Can have a set of prereqs that get auto passed down to the children events/stuff
- etc

Disadvantages of Series Table:
- Yet another table.
- Joins/DB queries could be more complicated
- Now you need to deal with archetypes of not only the events but also the series.
- If you have requirements as part of the series and someone clones and event from the series and not the whole series, how do you choose what depeedences and the like are carried over, or do you just make it fancy and handle it on the frontend by giving them checkboxes to choose what parts to carry over?

# Features

- Cart style signup for clases/multi class signup
- Text/Email based cancellation
- Class waitlist - if someone cancels, grab the next first waitlisted person and offer them if they response
- Allow teachers to select a choice to give a certain number of seats firsto ption to previously waitlisted people
  - Couple ways to do this.... Allow people to waitlist via archetype, or via a specific previous class..not sure which isbetter
