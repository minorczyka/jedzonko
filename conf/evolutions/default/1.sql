# --- !Ups

-- object: public."user" | type: TABLE --
-- DROP TABLE IF EXISTS public."user" CASCADE;
CREATE TABLE public."user"(
	id serial NOT NULL,
	provider_id text NOT NULL,
	provider_key text NOT NULL,
	first_name text,
	last_name text,
	full_name text,
	email text,
	avatar_url text,
	privileges smallint NOT NULL,
	CONSTRAINT user_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public."user" OWNER TO postgres;
-- ddl-end --

-- object: public.password_info | type: TABLE --
-- DROP TABLE IF EXISTS public.password_info CASCADE;
CREATE TABLE public.password_info(
	id serial NOT NULL,
	hasher text NOT NULL,
	password text NOT NULL,
	salt text,
	reset_token text,
	user_id integer NOT NULL,
	CONSTRAINT password_info_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public.password_info OWNER TO postgres;
-- ddl-end --

-- object: public.oauth2_info | type: TABLE --
-- DROP TABLE IF EXISTS public.oauth2_info CASCADE;
CREATE TABLE public.oauth2_info(
	id serial NOT NULL,
	access_token text NOT NULL,
	token_type text,
	expires_in integer,
	refresh_token text,
	user_id integer NOT NULL,
	CONSTRAINT oauth2_info_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public.oauth2_info OWNER TO postgres;
-- ddl-end --

-- object: public."group" | type: TABLE --
-- DROP TABLE IF EXISTS public."group" CASCADE;
CREATE TABLE public."group"(
	id serial NOT NULL,
	owner_id integer NOT NULL,
	name text NOT NULL,
	password text NOT NULL,
	creation_date timestamp NOT NULL,
	CONSTRAINT group_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public."group" OWNER TO postgres;
-- ddl-end --

-- object: public.user_to_group | type: TABLE --
-- DROP TABLE IF EXISTS public.user_to_group CASCADE;
CREATE TABLE public.user_to_group(
	id serial NOT NULL,
	user_id integer NOT NULL,
	group_id integer NOT NULL,
	CONSTRAINT user_to_group_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public.user_to_group OWNER TO postgres;
-- ddl-end --

-- object: public.voting | type: TABLE --
-- DROP TABLE IF EXISTS public.voting CASCADE;
CREATE TABLE public.voting(
	id serial NOT NULL,
	group_id integer NOT NULL,
	author_id integer NOT NULL,
	status smallint NOT NULL,
	creation_date timestamp NOT NULL,
	voting_end timestamp,
	order_end timestamp,
	CONSTRAINT voting_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public.voting OWNER TO postgres;
-- ddl-end --

-- object: public.place | type: TABLE --
-- DROP TABLE IF EXISTS public.place CASCADE;
CREATE TABLE public.place(
	id serial NOT NULL,
	group_id integer NOT NULL,
	author_id integer NOT NULL,
	name text NOT NULL,
	url text,
	minimum_order real,
	delivery_cost real,
	is_deleted bool NOT NULL DEFAULT false,
	CONSTRAINT place_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public.place OWNER TO postgres;
-- ddl-end --

-- object: public.user_to_voting | type: TABLE --
-- DROP TABLE IF EXISTS public.user_to_voting CASCADE;
CREATE TABLE public.user_to_voting(
	id serial NOT NULL,
	user_id integer NOT NULL,
	vote_id integer NOT NULL,
	place_id integer NOT NULL,
	CONSTRAINT user_to_voting_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public.user_to_voting OWNER TO postgres;
-- ddl-end --

-- object: public."order" | type: TABLE --
-- DROP TABLE IF EXISTS public."order" CASCADE;
CREATE TABLE public."order"(
	id serial NOT NULL,
	group_id integer NOT NULL,
	author_id integer NOT NULL,
	place_id integer NOT NULL,
	status smallint NOT NULL,
	creation_date timestamp NOT NULL,
	order_end timestamp,
	discount real,
	additional_cost real,
	CONSTRAINT order_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public."order" OWNER TO postgres;
-- ddl-end --

-- object: public.user_to_order | type: TABLE --
-- DROP TABLE IF EXISTS public.user_to_order CASCADE;
CREATE TABLE public.user_to_order(
	id serial NOT NULL,
	order_id integer NOT NULL,
	user_id integer NOT NULL,
	subject text NOT NULL,
	additional_info text,
	cost real,
	status smallint NOT NULL,
	CONSTRAINT user_to_order_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public.user_to_order OWNER TO postgres;
-- ddl-end --

-- object: public.order_message | type: TABLE --
-- DROP TABLE IF EXISTS public.order_message CASCADE;
CREATE TABLE public.order_message(
	id serial NOT NULL,
	order_id integer NOT NULL,
	message text NOT NULL,
	creation_date timestamp NOT NULL,
	CONSTRAINT order_message_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public.order_message OWNER TO postgres;
-- ddl-end --

-- object: public.prepaid | type: TABLE --
-- DROP TABLE IF EXISTS public.prepaid CASCADE;
CREATE TABLE public.prepaid(
	id serial NOT NULL,
	receiver_id integer NOT NULL,
	payer_id integer NOT NULL,
	order_id integer,
	amount real NOT NULL,
	transaction_date timestamp NOT NULL,
	type smallint NOT NULL,
	CONSTRAINT prepaid_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public.prepaid OWNER TO postgres;
-- ddl-end --

-- object: public.place_comment | type: TABLE --
-- DROP TABLE IF EXISTS public.place_comment CASCADE;
CREATE TABLE public.place_comment(
	id serial NOT NULL,
	author_id integer NOT NULL,
	place_id integer NOT NULL,
	comment text NOT NULL,
	creation_date timestamp NOT NULL,
	CONSTRAINT place_comment_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public.place_comment OWNER TO postgres;
-- ddl-end --

-- object: public.voting_to_place | type: TABLE --
-- DROP TABLE IF EXISTS public.voting_to_place CASCADE;
CREATE TABLE public.voting_to_place(
	id serial NOT NULL,
	voting_id integer NOT NULL,
	place_id integer NOT NULL,
	CONSTRAINT voting_to_place_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE public.voting_to_place OWNER TO postgres;
-- ddl-end --

-- object: password_info_user_id_fk | type: CONSTRAINT --
-- ALTER TABLE public.password_info DROP CONSTRAINT IF EXISTS password_info_user_id_fk CASCADE;
ALTER TABLE public.password_info ADD CONSTRAINT password_info_user_id_fk FOREIGN KEY (user_id)
REFERENCES public."user" (id) MATCH FULL
ON DELETE CASCADE ON UPDATE NO ACTION;
-- ddl-end --

-- object: oauth2_info_user_id_fk | type: CONSTRAINT --
-- ALTER TABLE public.oauth2_info DROP CONSTRAINT IF EXISTS oauth2_info_user_id_fk CASCADE;
ALTER TABLE public.oauth2_info ADD CONSTRAINT oauth2_info_user_id_fk FOREIGN KEY (user_id)
REFERENCES public."user" (id) MATCH FULL
ON DELETE CASCADE ON UPDATE NO ACTION;
-- ddl-end --

-- object: group_owner_id_fk | type: CONSTRAINT --
-- ALTER TABLE public."group" DROP CONSTRAINT IF EXISTS group_owner_id_fk CASCADE;
ALTER TABLE public."group" ADD CONSTRAINT group_owner_id_fk FOREIGN KEY (owner_id)
REFERENCES public."user" (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE NO ACTION;
-- ddl-end --

-- object: user_to_group_user_id_fk | type: CONSTRAINT --
-- ALTER TABLE public.user_to_group DROP CONSTRAINT IF EXISTS user_to_group_user_id_fk CASCADE;
ALTER TABLE public.user_to_group ADD CONSTRAINT user_to_group_user_id_fk FOREIGN KEY (user_id)
REFERENCES public."user" (id) MATCH FULL
ON DELETE CASCADE ON UPDATE NO ACTION;
-- ddl-end --

-- object: user_to_group_group_id | type: CONSTRAINT --
-- ALTER TABLE public.user_to_group DROP CONSTRAINT IF EXISTS user_to_group_group_id CASCADE;
ALTER TABLE public.user_to_group ADD CONSTRAINT user_to_group_group_id FOREIGN KEY (group_id)
REFERENCES public."group" (id) MATCH FULL
ON DELETE CASCADE ON UPDATE NO ACTION;
-- ddl-end --

-- object: voting_group_id_fk | type: CONSTRAINT --
-- ALTER TABLE public.voting DROP CONSTRAINT IF EXISTS voting_group_id_fk CASCADE;
ALTER TABLE public.voting ADD CONSTRAINT voting_group_id_fk FOREIGN KEY (group_id)
REFERENCES public."group" (id) MATCH FULL
ON DELETE CASCADE ON UPDATE NO ACTION;
-- ddl-end --

-- object: voting_author_id_fk | type: CONSTRAINT --
-- ALTER TABLE public.voting DROP CONSTRAINT IF EXISTS voting_author_id_fk CASCADE;
ALTER TABLE public.voting ADD CONSTRAINT voting_author_id_fk FOREIGN KEY (author_id)
REFERENCES public."user" (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE NO ACTION;
-- ddl-end --

-- object: place_group_id_fk | type: CONSTRAINT --
-- ALTER TABLE public.place DROP CONSTRAINT IF EXISTS place_group_id_fk CASCADE;
ALTER TABLE public.place ADD CONSTRAINT place_group_id_fk FOREIGN KEY (group_id)
REFERENCES public."group" (id) MATCH FULL
ON DELETE CASCADE ON UPDATE NO ACTION;
-- ddl-end --

-- object: place_author_id_fk | type: CONSTRAINT --
-- ALTER TABLE public.place DROP CONSTRAINT IF EXISTS place_author_id_fk CASCADE;
ALTER TABLE public.place ADD CONSTRAINT place_author_id_fk FOREIGN KEY (author_id)
REFERENCES public."user" (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE NO ACTION;
-- ddl-end --

-- object: user_to_voting_user_id_fk | type: CONSTRAINT --
-- ALTER TABLE public.user_to_voting DROP CONSTRAINT IF EXISTS user_to_voting_user_id_fk CASCADE;
ALTER TABLE public.user_to_voting ADD CONSTRAINT user_to_voting_user_id_fk FOREIGN KEY (user_id)
REFERENCES public."user" (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE NO ACTION;
-- ddl-end --

-- object: user_to_voting_vote_id_fk | type: CONSTRAINT --
-- ALTER TABLE public.user_to_voting DROP CONSTRAINT IF EXISTS user_to_voting_vote_id_fk CASCADE;
ALTER TABLE public.user_to_voting ADD CONSTRAINT user_to_voting_vote_id_fk FOREIGN KEY (vote_id)
REFERENCES public.voting (id) MATCH FULL
ON DELETE CASCADE ON UPDATE NO ACTION;
-- ddl-end --

-- object: user_to_voting_place_id_fk | type: CONSTRAINT --
-- ALTER TABLE public.user_to_voting DROP CONSTRAINT IF EXISTS user_to_voting_place_id_fk CASCADE;
ALTER TABLE public.user_to_voting ADD CONSTRAINT user_to_voting_place_id_fk FOREIGN KEY (place_id)
REFERENCES public.place (id) MATCH FULL
ON DELETE CASCADE ON UPDATE NO ACTION;
-- ddl-end --

-- object: order_group_id_fk | type: CONSTRAINT --
-- ALTER TABLE public."order" DROP CONSTRAINT IF EXISTS order_group_id_fk CASCADE;
ALTER TABLE public."order" ADD CONSTRAINT order_group_id_fk FOREIGN KEY (group_id)
REFERENCES public."group" (id) MATCH FULL
ON DELETE CASCADE ON UPDATE NO ACTION;
-- ddl-end --

-- object: order_place_id_fk | type: CONSTRAINT --
-- ALTER TABLE public."order" DROP CONSTRAINT IF EXISTS order_place_id_fk CASCADE;
ALTER TABLE public."order" ADD CONSTRAINT order_place_id_fk FOREIGN KEY (place_id)
REFERENCES public.place (id) MATCH FULL
ON DELETE CASCADE ON UPDATE NO ACTION;
-- ddl-end --

-- object: order_author_id_fk | type: CONSTRAINT --
-- ALTER TABLE public."order" DROP CONSTRAINT IF EXISTS order_author_id_fk CASCADE;
ALTER TABLE public."order" ADD CONSTRAINT order_author_id_fk FOREIGN KEY (author_id)
REFERENCES public."user" (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE NO ACTION;
-- ddl-end --

-- object: user_to_order_order_id_fk | type: CONSTRAINT --
-- ALTER TABLE public.user_to_order DROP CONSTRAINT IF EXISTS user_to_order_order_id_fk CASCADE;
ALTER TABLE public.user_to_order ADD CONSTRAINT user_to_order_order_id_fk FOREIGN KEY (order_id)
REFERENCES public."order" (id) MATCH FULL
ON DELETE CASCADE ON UPDATE NO ACTION;
-- ddl-end --

-- object: user_to_order_user_id_fk | type: CONSTRAINT --
-- ALTER TABLE public.user_to_order DROP CONSTRAINT IF EXISTS user_to_order_user_id_fk CASCADE;
ALTER TABLE public.user_to_order ADD CONSTRAINT user_to_order_user_id_fk FOREIGN KEY (user_id)
REFERENCES public."user" (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE NO ACTION;
-- ddl-end --

-- object: order_message_order_id_fk | type: CONSTRAINT --
-- ALTER TABLE public.order_message DROP CONSTRAINT IF EXISTS order_message_order_id_fk CASCADE;
ALTER TABLE public.order_message ADD CONSTRAINT order_message_order_id_fk FOREIGN KEY (order_id)
REFERENCES public."order" (id) MATCH FULL
ON DELETE CASCADE ON UPDATE NO ACTION;
-- ddl-end --

-- object: prepaid_receiver_fk | type: CONSTRAINT --
-- ALTER TABLE public.prepaid DROP CONSTRAINT IF EXISTS prepaid_receiver_fk CASCADE;
ALTER TABLE public.prepaid ADD CONSTRAINT prepaid_receiver_fk FOREIGN KEY (receiver_id)
REFERENCES public."user" (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE NO ACTION;
-- ddl-end --

-- object: prepaid_payer_fk | type: CONSTRAINT --
-- ALTER TABLE public.prepaid DROP CONSTRAINT IF EXISTS prepaid_payer_fk CASCADE;
ALTER TABLE public.prepaid ADD CONSTRAINT prepaid_payer_fk FOREIGN KEY (payer_id)
REFERENCES public."user" (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE NO ACTION;
-- ddl-end --

-- object: prepaid_order_fk | type: CONSTRAINT --
-- ALTER TABLE public.prepaid DROP CONSTRAINT IF EXISTS prepaid_order_fk CASCADE;
ALTER TABLE public.prepaid ADD CONSTRAINT prepaid_order_fk FOREIGN KEY (order_id)
REFERENCES public.user_to_order (id) MATCH FULL
ON DELETE SET NULL ON UPDATE NO ACTION;
-- ddl-end --

-- object: place_comment_author_fk | type: CONSTRAINT --
-- ALTER TABLE public.place_comment DROP CONSTRAINT IF EXISTS place_comment_author_fk CASCADE;
ALTER TABLE public.place_comment ADD CONSTRAINT place_comment_author_fk FOREIGN KEY (author_id)
REFERENCES public."user" (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE NO ACTION;
-- ddl-end --

-- object: place_comment_place_fk | type: CONSTRAINT --
-- ALTER TABLE public.place_comment DROP CONSTRAINT IF EXISTS place_comment_place_fk CASCADE;
ALTER TABLE public.place_comment ADD CONSTRAINT place_comment_place_fk FOREIGN KEY (place_id)
REFERENCES public.place (id) MATCH FULL
ON DELETE CASCADE ON UPDATE NO ACTION;
-- ddl-end --

-- object: voting_to_place_voting_id_fk | type: CONSTRAINT --
-- ALTER TABLE public.voting_to_place DROP CONSTRAINT IF EXISTS voting_to_place_voting_id_fk CASCADE;
ALTER TABLE public.voting_to_place ADD CONSTRAINT voting_to_place_voting_id_fk FOREIGN KEY (voting_id)
REFERENCES public.voting (id) MATCH FULL
ON DELETE CASCADE ON UPDATE NO ACTION;
-- ddl-end --

-- object: voting_to_place_place_id_fk | type: CONSTRAINT --
-- ALTER TABLE public.voting_to_place DROP CONSTRAINT IF EXISTS voting_to_place_place_id_fk CASCADE;
ALTER TABLE public.voting_to_place ADD CONSTRAINT voting_to_place_place_id_fk FOREIGN KEY (place_id)
REFERENCES public.place (id) MATCH FULL
ON DELETE CASCADE ON UPDATE NO ACTION;
-- ddl-end --

# --- !Downs