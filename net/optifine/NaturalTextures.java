package net.optifine;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.src.OFConfig;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.optifine.util.TextureUtils;

public class NaturalTextures
{
    private static NaturalProperties[] propertiesByIndex = new NaturalProperties[0];

    public static void update()
    {
        propertiesByIndex = new NaturalProperties[0];

        if (OFConfig.isNaturalTextures())
        {
            String s = "optifine/natural.properties";

            try
            {
                ResourceLocation resourcelocation = new ResourceLocation(s);

                if (!OFConfig.hasResource(resourcelocation))
                {
                    OFConfig.dbg("NaturalTextures: configuration \"" + s + "\" not found");
                    return;
                }

                boolean flag = OFConfig.isFromDefaultResourcePack(resourcelocation);
                InputStream inputstream = OFConfig.getResourceStream(resourcelocation);
                ArrayList arraylist = new ArrayList(256);
                String s1 = OFConfig.readInputStream(inputstream);
                inputstream.close();
                String[] astring = OFConfig.tokenize(s1, "\n\r");

                if (flag)
                {
                    OFConfig.dbg("Natural Textures: Parsing default configuration \"" + s + "\"");
                    OFConfig.dbg("Natural Textures: Valid only for textures from default resource pack");
                }
                else
                {
                    OFConfig.dbg("Natural Textures: Parsing configuration \"" + s + "\"");
                }

                TextureMap texturemap = TextureUtils.getTextureMapBlocks();

                for (int i = 0; i < astring.length; ++i)
                {
                    String s2 = astring[i].trim();

                    if (!s2.startsWith("#"))
                    {
                        String[] astring1 = OFConfig.tokenize(s2, "=");

                        if (astring1.length != 2)
                        {
                            OFConfig.warn("Natural Textures: Invalid \"" + s + "\" line: " + s2);
                        }
                        else
                        {
                            String s3 = astring1[0].trim();
                            String s4 = astring1[1].trim();
                            TextureAtlasSprite textureatlassprite = texturemap.getSpriteSafe("minecraft:blocks/" + s3);

                            if (textureatlassprite == null)
                            {
                                OFConfig.warn("Natural Textures: Texture not found: \"" + s + "\" line: " + s2);
                            }
                            else
                            {
                                int j = textureatlassprite.getIndexInMap();

                                if (j < 0)
                                {
                                    OFConfig.warn("Natural Textures: Invalid \"" + s + "\" line: " + s2);
                                }
                                else
                                {
                                    if (flag && !OFConfig.isFromDefaultResourcePack(new ResourceLocation("textures/blocks/" + s3 + ".png")))
                                    {
                                        return;
                                    }

                                    NaturalProperties naturalproperties = new NaturalProperties(s4);

                                    if (naturalproperties.isValid())
                                    {
                                        while (arraylist.size() <= j)
                                        {
                                            arraylist.add(null);
                                        }

                                        arraylist.set(j, naturalproperties);
                                        OFConfig.dbg("NaturalTextures: " + s3 + " = " + s4);
                                    }
                                }
                            }
                        }
                    }
                }

                propertiesByIndex = (NaturalProperties[])((NaturalProperties[])arraylist.toArray(new NaturalProperties[arraylist.size()]));
            }
            catch (FileNotFoundException var17)
            {
                OFConfig.warn("NaturalTextures: configuration \"" + s + "\" not found");
                return;
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
    }

    public static BakedQuad getNaturalTexture(BlockPos blockPosIn, BakedQuad quad)
    {
        TextureAtlasSprite textureatlassprite = quad.getSprite();

        if (textureatlassprite == null)
        {
            return quad;
        }
        else
        {
            NaturalProperties naturalproperties = getNaturalProperties(textureatlassprite);

            if (naturalproperties == null)
            {
                return quad;
            }
            else
            {
                int i = ConnectedTextures.getSide(quad.getFace());
                int j = OFConfig.getRandom(blockPosIn, i);
                int k = 0;
                boolean flag = false;

                if (naturalproperties.rotation > 1)
                {
                    k = j & 3;
                }

                if (naturalproperties.rotation == 2)
                {
                    k = k / 2 * 2;
                }

                if (naturalproperties.flip)
                {
                    flag = (j & 4) != 0;
                }

                return naturalproperties.getQuad(quad, k, flag);
            }
        }
    }

    public static NaturalProperties getNaturalProperties(TextureAtlasSprite icon)
    {
        if (!(icon instanceof TextureAtlasSprite))
        {
            return null;
        }
        else
        {
            int i = icon.getIndexInMap();

            if (i >= 0 && i < propertiesByIndex.length)
            {
                NaturalProperties naturalproperties = propertiesByIndex[i];
                return naturalproperties;
            }
            else
            {
                return null;
            }
        }
    }
}
