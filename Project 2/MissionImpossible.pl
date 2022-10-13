:- consult("./KB.pl").
/*
ethan_loc is a successor state axiom that 
checks if ethan is in location (X,Y) in the 
given state

Base case is if the state is s0 then the 
position should be the initial position
*/
ethan_loc(X,Y,s0):-
	ethan_loc(X,Y).
/*
The successor state axiom is divided into 2 parts
First Part (Wasn't in location and moved to location):
	1.Ethan was in (X,Y+1) and moved right (A=right)
	2.Ethat was in (X,Y-1) and moved left (A=left)
	3.Ethan was in (X-1,Y) and moved up (A=up)
	4.Ethan was in (X+1,Y) and moved down (A=down)
Second Part (Was in location and didn't change):
	1.Ethan was in (X,Y) and performed a drop (A=drop)
	2.Ethan was in (X,Y) and performed a carry (A=carry)
*/
ethan_loc(X,Y,result(A,S)):-
	(((A = right,ethan_loc(X,Y1,S),Y is Y1+1);
	(A = left,ethan_loc(X,Y1,S),Y is Y1-1);
	(A = up,ethan_loc(X1,Y,S),X is X1-1);
	(A = down,ethan_loc(X1,Y,S),X is X1+1));
	((A = drop,ethan_loc(X,Y,S));
	((A = carry,ethan_loc(X,Y,S))))).
/*

Capacity is another successor state axiom that
checks if the capcaity in the given state is C

Base case is if the state is s0 then the capacity
is equal to the initial capcaity
*/
capacity(C,s0):-
	capacity(C).
/*
The successor state axiom is divided into 2 parts
First Part (Capacity wasn't C but became C):
	1.Capacity was C-1 and a carry operation was performed
	making sure that the current location has a member and 
	the member isn't already saved or already carried and the 
	capacity was greater than -1 meaning that there is space 
	to carry a member
	2.Capactiy wasn't C but a drop operation caused
	it to be C, making sure that ethan is at the drop
	location and we are actually carrying at least 1 
	member.
Second Part (Capacity was C and action didn't change C):
	1.Capacity was already C and the action 
	performed was a move action.
	
*/
capacity(C,result(A,S)):-
	((
	(A = carry,ethan_loc(X,Y,S),members_loc(Q),member([X,Y],Q),getCarriedMembers(S,M),\+member([X,Y],M),getSavedMembers(S,R),\+member([X,Y],R),capacity(C1,S),C is C1-1,C > -1);
	(A = drop,ethan_loc(X,Y,S),submarine(X,Y),capacity(C),capacity(C1,S),C1<C));
	((A = right;A = left;A = up;A = down),capacity(C,S))).
/*
Next is a helper to check if 2 arrays are of 
equal size.
*/
arrayLengthEqual([],[]).
arrayLengthEqual([_|B],[_|D]):-
	arrayLengthEqual(B,D).
/*
Goal predicate is the required predicate and it only
calls a helper predicate to perform IDS
*/	
goal(S):-
	goalHelper(S, 1).
/*
goalHelper is a predicate that given the depth limit
performs depth limited search on the function checkGoal.
It checks if the previous depth limited search was 
able to find a solution; if yes, it returns the 
answer, if no it recursively calls goalHelper again 
with a higher depth limit. 
*/
goalHelper(S, D):-
	call_with_depth_limit(checkGoal(S), D, R),
	((R \= depth_limit_exceeded);
	(R = depth_limit_exceeded,
	D1 is D+1,
	goalHelper(S, D1))).	
/*
Check goal is a predicate that checks if the given
state is a goal state.

For a state to be a goal state we need to check that
all members are saved. To do that we get the saved 
members and the array of members locations and make
sure that both are of equal size. We are sure that 
saved members always exist and that a member cannot
be saved twice or carried twice in the getSavedMembers
predicate. Finally we make sure that ethan is in the
same location as the submarine.
*/		
checkGoal(S):-
	getSavedMembers(S,M),
	members_loc(Q),
	arrayLengthEqual(M,Q),
	ethan_loc(X,Y,S),
	submarine(X,Y).
/*
Helper predicate to get the intersection between 2
arrays.
*/
intersection([],_,[]).
intersection([A|B],C,[A|R]):-
	member(A,C),
	intersection(B,C,R).
intersection([A|B],C,R):-
	\+member(A,C),
	intersection(B,C,R).
/*
Helper predicate that gets the saved members in a
given state. 

If the state is s0 then there are no saved members.
*/
getSavedMembers(s0,[]).
/*
If Ethan is at the same location as the submarine
and he performs a drop, then we get the carried 
members and add them to the saved members making sure
that there are no carried members that were already
saved. (Intersection between carried and saved is empty)
*/
getSavedMembers(result(drop,S),[C|R]):-
	ethan_loc(X,Y,S),
	submarine(X,Y),
	getCarriedMembers(S,[C]),
	getSavedMembers(S,R),
	intersection(C,R,[]).
/*
If the last action in the current state was a 
movement action then we simply call getSaved members 
recursively with the rest of the state.
*/
getSavedMembers(result(A,S),R):-
	(A=up;A=down;A=left;A=right),
	getSavedMembers(S,R).
/*
If the last action in the current state was a carry,
then we make sure that there is a member to carry,
and that there is capacity for this member to be 
carried then we recursively call getSavedMembers
with the rest of the state.
*/
getSavedMembers(result(carry,S),R):-
	members_loc(Q),
	ethan_loc(X,Y,S),
	member([X,Y],Q),
	capacity(C,S),
	C > 0,
	getSavedMembers(S,R).
/*
getCarriedMembers is a helper predicate to get the
carried members in a given state.

If the state is s0 then it is clear that no members 
are carried.
*/
getCarriedMembers(s0,[]).
/*
If the last action in the current state was a carry
then we check if there is a member in the current
position and there is enough capacity to carry.
We also check that the member we are trying to carry
was not previously carried or saved.
*/
getCarriedMembers(result(carry,S),[[X,Y]|R]):-
	ethan_loc(X,Y,S),
	members_loc(Q),
	member([X,Y],Q),
	capacity(C,S),
	C>0,
	getCarriedMembers(S,R),
	\+member([X,Y],R),
	getSavedMembers(S, F),
	\+member([X,Y],F).
/*
If the last action in the current state was a drop,
then we check if we had any carried members and then
set the carried members to an empty array.
*/
getCarriedMembers(result(drop,S),[]):-
	capacity(C, S),
	capacity(C2),
	C < C2.
/*
Finally if the last action in the current state was 
a movement action, then we recursively call getCarriedMembers
on the rest of the state.
*/
getCarriedMembers(result(A,S),R):-
	(A=up;A=down;A=left;A=right),
	getCarriedMembers(S,R).