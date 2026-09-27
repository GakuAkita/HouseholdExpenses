import { AllMailType } from "../../type/Mailbox";
import { MailSource } from "../mailSource";
import { amazonItemSource } from "./amazonItem";
import { amazonKindleSource } from "./amazonKindle";
import { amazonSubscribeSource } from "./amazonSubscribe";
import { rakutenCardEtcSource } from "./rakutenCardEtc";
import { rakutenPaySource } from "./rakutenPay";
import { shikokuElectricPowerSource } from "./shikokuElectricPower";
import { udemySource } from "./udemy";

type SourcesByNodeName = {
  [N in AllMailType["nodeName"]]: MailSource<Extract<AllMailType, { nodeName: N }>>;
};

/**
 * The source for each mail type. Adding a type to AllMailType without a source here
 * is a compile error.
 */
const mailSources: SourcesByNodeName = {
  rakuten_pay: rakutenPaySource,
  amazon_kindle: amazonKindleSource,
  shikoku_electric_power: shikokuElectricPowerSource,
  amazon_item: amazonItemSource,
  amazon_subscribe: amazonSubscribeSource,
  udemy: udemySource,
  rakuten_card_etc: rakutenCardEtcSource,
};

/** The source for the setting's mail type, or undefined for a node name the code doesn't know. */
export const mailSourceFor = <S extends AllMailType>(setting: S): MailSource<S> | undefined =>
  /* SourcesByNodeName guarantees that the source for a node name handles that setting type. */
  (mailSources as unknown as Record<string, MailSource<S> | undefined>)[setting.nodeName];
